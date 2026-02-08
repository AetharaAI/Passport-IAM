/**
 * https://passport-pro.ai/docs-api/11.0/rest-api/index.html#_componentexportrepresentation
 */

export default interface ComponentExportRepresentation {
  id?: string;
  name?: string;
  providerId?: string;
  subType?: string;
  subComponents?: { [index: string]: ComponentExportRepresentation };
  config?: { [index: string]: string };
}
