import { Spinner } from "@patternfly/react-core";
import Passport from "keycloak-js";
import {
  PropsWithChildren,
  createContext,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { AlertProvider } from "../alerts/Alerts";
import { ErrorPage } from "./ErrorPage";
import { Help } from "./HelpContext";
import { BaseEnvironment } from "./environment";

export type PassportContext<T extends BaseEnvironment = BaseEnvironment> =
  PassportContextProps<T> & {
    passport: Passport;
  };

const createPassportEnvContext = <T extends BaseEnvironment>() =>
  createContext<PassportContext<T> | undefined>(undefined);

let PassportEnvContext: any;

export const useEnvironment = <
  T extends BaseEnvironment = BaseEnvironment,
>() => {
  const context = useContext<PassportContext<T>>(PassportEnvContext);
  if (!context)
    throw Error(
      "no environment provider in the hierarchy make sure to add the provider",
    );
  return context;
};

interface PassportContextProps<T extends BaseEnvironment> {
  environment: T;
}

export const PassportProvider = <T extends BaseEnvironment>({
  environment,
  children,
}: PropsWithChildren<PassportContextProps<T>>) => {
  PassportEnvContext = createPassportEnvContext<T>();
  const calledOnce = useRef(false);
  const [init, setInit] = useState(false);
  const [error, setError] = useState<unknown>();
  const passport = useMemo(() => {
    const passport = new Passport({
      url: environment.serverBaseUrl,
      realm: environment.realm,
      clientId: environment.clientId,
    });

    passport.onAuthLogout = () => passport.login();

    return passport;
  }, [environment]);

  useEffect(() => {
    // only needed in dev mode
    if (calledOnce.current) {
      return;
    }

    const init = () =>
      passport.init({
        onLoad: "login-required",
        pkceMethod: "S256",
        responseMode: "query",
        scope: environment.scope,
      });

    init()
      .then(() => setInit(true))
      .catch((error) => setError(error));

    calledOnce.current = true;
  }, [passport]);

  if (error) {
    return <ErrorPage error={error} />;
  }

  if (!init) {
    return <Spinner />;
  }

  return (
    <PassportEnvContext.Provider value={{ environment, passport }}>
      <AlertProvider>
        <Help>{children}</Help>
      </AlertProvider>
    </PassportEnvContext.Provider>
  );
};
