package org.passport.storage;

import java.util.stream.Stream;

import org.passport.cluster.ClusterEvent;
import org.passport.cluster.ClusterListener;
import org.passport.cluster.ClusterProvider;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.StorageProviderRealmModel;
import org.passport.models.utils.PostMigrationEvent;
import org.passport.provider.ProviderEvent;
import org.passport.provider.ProviderEventListener;
import org.passport.storage.UserStorageProviderModel.SyncMode;
import org.passport.storage.user.ImportSynchronization;

import org.jboss.logging.Logger;

import static org.passport.models.utils.PassportModelUtils.runJobInTransaction;

public final class UserStorageEventListener implements ClusterListener, ProviderEventListener {

    private static final Logger logger = Logger.getLogger(UserStorageEventListener.class);
    private static final String USER_STORAGE_TASK_KEY = "user-storage";

    private final PassportSessionFactory sessionFactory;

    public UserStorageEventListener(PassportSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void eventReceived(ClusterEvent event) {
        UserStorageProviderClusterEvent fedEvent = (UserStorageProviderClusterEvent) event;
        String realmId = fedEvent.getRealmId();

        runJobInTransaction(sessionFactory, session -> {
            RealmModel realm = session.realms().getRealm(realmId);

            if (realm == null) {
                throw new RuntimeException("Failed to execute session task. Realm with id " + realmId + " not found.");
            }

            session.getContext().setRealm(realm);
            refreshScheduledTasks(session, fedEvent.getStorageProvider(), fedEvent.isRemoved());
        });
    }

    @Override
    public void onEvent(ProviderEvent event) {
        if (event instanceof PostMigrationEvent) {
            runJobInTransaction(sessionFactory, session -> {
                session.realms().getRealmsWithProviderTypeStream(UserStorageProvider.class)
                        .forEach(realm -> {
                            try {
                                session.getContext().setRealm(realm);
                                getUserStorageProvidersStream(realm).forEachOrdered(provider -> reScheduleTasks(session, provider));
                            } finally {
                                session.getContext().setRealm(null);
                            }
                });

                ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);

                if (clusterProvider != null) {
                    clusterProvider.registerListener(USER_STORAGE_TASK_KEY, this);
                }
            });
        } else if (event instanceof StoreSyncEvent ev) {
            UserStorageProviderModel model = ev.getModel() == null ? null: new UserStorageProviderModel(ev.getModel());
            boolean removed = ev.getRemoved();
            String realmId = ev.getRealm().getId();

            runJobInTransaction(sessionFactory, session -> {
                RealmModel realm = session.realms().getRealm(realmId);
                if (realm == null) {
                    return;
                }
                session.getContext().setRealm(realm);

                if (model != null) {
                    refreshScheduledTasks(session, model, removed);
                    notifyStoreSyncClusterUpdate(session, realm, model, removed);
                } else {
                    getUserStorageProvidersStream(realm).forEachOrdered(fedProvider -> {
                        refreshScheduledTasks(session, fedProvider, removed);
                        notifyStoreSyncClusterUpdate(session, realm, fedProvider, removed);
                    });
                }
            });
        }
    }

    private void reScheduleTasks(PassportSession session, UserStorageProviderModel provider) {
        PassportSessionFactory sessionFactory = session.getPassportSessionFactory();
        UserStorageProviderFactory<?> factory = (UserStorageProviderFactory<?>) sessionFactory.getProviderFactory(UserStorageProvider.class, provider.getProviderId());
        RealmModel realm = session.getContext().getRealm();

        if (!(factory instanceof ImportSynchronization)) {
            logger.debugf("Not refreshing periodic sync settings for provider '%s' in realm '%s'", provider.getName(), realm.getName());
            return;
        }

        logger.debugf("Going to refresh periodic sync settings for provider '%s' in realm '%s' with realmId '%s'. Full sync period: %d , changed users sync period: %d",
                provider.getName(), realm.getName(), realm.getId(), provider.getFullSyncPeriod(), provider.getChangedSyncPeriod());
        scheduleTask(session, provider, SyncMode.FULL);
        scheduleTask(session, provider, SyncMode.CHANGED);
    }

    private void scheduleTask(PassportSession session, UserStorageProviderModel provider, SyncMode mode) {
        UserStorageSyncTask task = new UserStorageSyncTask(provider, mode);

        if (!task.schedule(session)) {
            // cancel potentially dangling task
            task.cancel(session);
        }
    }

    // Ensure all cluster nodes are notified
    private void notifyStoreSyncClusterUpdate(PassportSession session, RealmModel realm, UserStorageProviderModel provider, boolean removed) {
        PassportSessionFactory sessionFactory = session.getPassportSessionFactory();
        UserStorageProviderFactory<?> factory = (UserStorageProviderFactory<?>) sessionFactory.getProviderFactory(UserStorageProvider.class, provider.getProviderId());

        if (!(factory instanceof ImportSynchronization)) {
            return;
        }

        ClusterProvider cp = session.getProvider(ClusterProvider.class);

        if (cp != null) {
            UserStorageProviderClusterEvent event = UserStorageProviderClusterEvent.createEvent(removed, realm.getId(), provider);
            cp.notify(USER_STORAGE_TASK_KEY, event, true);
        }
    }

    private void refreshScheduledTasks(PassportSession session, UserStorageProviderModel model, boolean removed) {
        if (removed) {
            new UserStorageSyncTask(model, SyncMode.FULL).cancel(session);
            new UserStorageSyncTask(model, SyncMode.CHANGED).cancel(session);
        } else {
            reScheduleTasks(session, model);
        }
    }

    private Stream<UserStorageProviderModel> getUserStorageProvidersStream(RealmModel realm) {
        if (realm instanceof StorageProviderRealmModel s) {
            return s.getUserStorageProvidersStream();
        }

        return Stream.empty();
    }
}
