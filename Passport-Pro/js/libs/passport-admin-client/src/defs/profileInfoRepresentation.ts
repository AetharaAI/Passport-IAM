/**
 * https://passport-pro.ai/docs-api/11.0/rest-api/index.html#_profileinforepresentation
 */
export default interface ProfileInfoRepresentation {
  name?: string;
  disabledFeatures?: string[];
  previewFeatures?: string[];
  experimentalFeatures?: string[];
}
