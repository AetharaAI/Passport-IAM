package org.passport.storage.ldap.mappers;

import org.passport.common.util.PemUtils;
import org.passport.component.ComponentModel;
import org.passport.storage.ldap.LDAPStorageProvider;
import org.passport.storage.ldap.idm.query.Condition;
import org.passport.storage.ldap.idm.query.internal.EqualCondition;
import org.passport.storage.ldap.idm.query.internal.LDAPQuery;

public class CertificateLDAPStorageMapper extends UserAttributeLDAPStorageMapper {

  public static final String IS_DER_FORMATTED = "is.der.formatted";

  public CertificateLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
    super(mapperModel, ldapProvider);
  }

  @Override
  public void beforeLDAPQuery(LDAPQuery query) {
    super.beforeLDAPQuery(query);

    String ldapAttrName = getLdapAttributeName();

    if (isDerFormatted()) {
      for (Condition condition : query.getConditions()) {
        if (condition instanceof EqualCondition &&
            condition.getParameterName().equalsIgnoreCase(ldapAttrName)) {
          EqualCondition equalCondition = ((EqualCondition) condition);
          equalCondition.setValue(PemUtils.pemToDer(equalCondition.getValue().toString()));
        }
      }
    }
  }

  private boolean isDerFormatted() {
    return mapperModel.get(IS_DER_FORMATTED, false);
  }
}
