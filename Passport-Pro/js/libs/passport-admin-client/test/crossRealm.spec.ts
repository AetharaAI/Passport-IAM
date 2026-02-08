// tslint:disable:no-unused-expression
import { faker } from "@faker-js/faker";
import * as chai from "chai";
import { PassportAdminClient } from "../src/client.js";
import { credentials } from "./constants.js";

const expect = chai.expect;

describe("Realms", () => {
  let kcAdminClient: PassportAdminClient;
  let currentRealmId: string;

  before(async () => {
    kcAdminClient = new PassportAdminClient();
    await kcAdminClient.auth(credentials);

    const realmId = faker.internet.username();
    const realm = await kcAdminClient.realms.create({
      id: realmId,
      realm: realmId,
    });
    expect(realm.realmName).to.be.ok;
    currentRealmId = realmId;
  });

  after(async () => {
    await kcAdminClient.realms.del({ realm: currentRealmId });
  });

  it("add a user to another realm", async () => {
    const username = faker.internet.username().toLowerCase();
    const user = await kcAdminClient.users.create({
      realm: currentRealmId,
      username,
      email: "test@passport-pro.ai",
      // enabled required to be true in order to send actions email
      emailVerified: true,
      enabled: true,
    });
    const foundUser = (await kcAdminClient.users.findOne({
      realm: currentRealmId,
      id: user.id,
    }))!;
    expect(foundUser.username).to.be.eql(username);
  });
});
