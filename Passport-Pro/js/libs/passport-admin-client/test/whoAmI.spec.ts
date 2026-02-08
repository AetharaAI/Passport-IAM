// tslint:disable:no-unused-expression
import * as chai from "chai";
import { PassportAdminClient } from "../src/client.js";
import { credentials } from "./constants.js";

const expect = chai.expect;

describe("Who am I", () => {
  let client: PassportAdminClient;

  before(async () => {
    client = new PassportAdminClient();
    await client.auth(credentials);
  });

  it.skip("list who I am", async () => {
    const whoAmI = await client.whoAmI.find();
    expect(whoAmI).to.be.ok;
    expect(whoAmI.displayName).to.be.equal("admin");
  });
});
