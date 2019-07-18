const { Api, JsonRpc, RpcError } = require('eosjs');
const JsSignatureProvider = require('eosjs/dist/eosjs-jssig').default;
const fetch = require('node-fetch');
const { TextEncoder, TextDecoder } = require('util');
const defaultPrivatekey = "privatekey";
const signatureProvider = new JsSignatureProvider([defaultPrivatekey]);
const rpc = new JsonRpc('https://eos.greymass.com:443', { fetch });
const api = new Api({ rpc, signatureProvider, textDecoder: new TextDecoder(), textEncoder: new TextEncoder() });
const endpoint = 'https://eos.greymass.com:443';


const config = {
  api: api,
  rpc: rpc,
  endpoint: endpoint
};


module.exports = config;
