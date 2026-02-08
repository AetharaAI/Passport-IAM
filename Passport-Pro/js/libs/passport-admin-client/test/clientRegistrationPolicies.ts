import * as chai from "chai";
import { PassportAdminClient } from "../src/client.js";
import { credentials } from "./constants.js";

const expect = chai.expect;

describe("Client Registration Policies", () => {
  let client: PassportAdminClient;

  before(async () => {
    client = new PassportAdminClient();
    await client.auth(credentials);
  });

  it("list client registration policies", async () => {
    const clientRegistrationPolicies =
      await client.clientRegistrationPolicies.find();
    expect(clientRegistrationPolicies).to.be.ok;
  });
});
