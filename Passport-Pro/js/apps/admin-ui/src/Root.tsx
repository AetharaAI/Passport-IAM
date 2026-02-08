import { PassportProvider } from "@passport/passport-ui-shared";

import { App } from "./App";
import { environment } from "./environment";

export const Root = () => (
  <PassportProvider environment={environment}>
    <App />
  </PassportProvider>
);
