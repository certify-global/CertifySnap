package com.certify.snap.qrverification;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.certify.snap.bean.QRCodeIssuer;
import com.certify.snap.controller.DatabaseController;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class JwtHelper {
    private String issuer;
    private String kid;
    public Payload payload;
    public static List<QRCodeIssuer> qrCodeIssuers=null;
    public static JwtHelper decode(String raw) throws DecodeException, IOException {
        JwtHelper instance = new JwtHelper();
        String truncated = raw.substring(5);
        String token = instance.convertHexToChar(truncated);
        String[] headerPayloadSignature =instance.decodeJwtPayload(token);
        Header header = Header.decode(headerPayloadSignature[0]);
        Payload payload = Payload.decode(headerPayloadSignature[1]);

        String issuer = payload.iss;
        String keysJson = instance.fetchKeys(issuer);
         qrCodeIssuers=DatabaseController.getInstance().getIssuerKey();
        Map<String, JsonWebKey> webKeyMap = JsonWebKey.Parse(keysJson);
        JsonWebKey key = webKeyMap.get(header.kid);
        Gson gson=new Gson();
        for(Map.Entry entry:webKeyMap.entrySet()){
            QRCodeIssuer qrCodeIssuer=new QRCodeIssuer();
            qrCodeIssuer.setKeyID(entry.getKey().toString());
            qrCodeIssuer.setIssuer(issuer);
            qrCodeIssuer.setContentValue(gson.toJson(entry.getValue()));
            if(!DatabaseController.getInstance().isuniqueKey(entry.getKey().toString())) {
                DatabaseController.getInstance().insertQRCodeIssuer(qrCodeIssuer);
            }
        }
        instance.payload =payload;
        return instance;
    }
    public boolean verify(){
        return true;
    }
    private String fetchKeys(String issuer) {

        String url = issuer+"/.well-known/jwks.json";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url).build();
        try(Response response = client.newCall(request).execute()){
            String json = response.body().string();
            return json;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class DecodeException extends  Exception{
        public DecodeException(String message) {
            super(message);
        }
    }

    private String[] decodeJwtPayload(String raw) throws DecodeException {
        if(raw == null) throw new DecodeException("input is null");
        String[] splitRaw = raw.split("\\.");
        if(splitRaw.length != 3)
            throw new DecodeException("invalid JWT, not in <header>.<payload>.<signature> format, length: "+splitRaw.length);
        String header = splitRaw[0];
        String payload = splitRaw[1];
        payload = payload.length() % 2 == 0 ?
                payload.concat("==") : payload.concat("=");
        payload = payload.replace('-', '+').replace('_','/');
        splitRaw[1] = payload;
        for(String el : splitRaw) {
            el = el.length() %2 == 0 ? el +"==" : el + "=";
        }
        return  splitRaw;
    }
    private String convertHexToChar(String truncated) {
        char[] src = truncated.toCharArray();
        char[] cb = new char[truncated.length()/2];
        for(int i = 0; i < truncated.length(); i+=2){
            try{
                int num = Integer.parseInt(new String(new char[]{src[i], src[i+1]}));
                cb[i/2] = (char)(num + 45);
            }catch (Exception ex){
                System.out.println(ex.toString());
            }
        }

        return new String(cb);
    }
    public  static class Header{
        public String zip;
        public  String alg;
        public  String kid;
        public  static Header decode(String base64) throws JsonProcessingException {
            byte[] data = android.util.Base64.decode(base64,0);
            base64 = new String(data);
            Header h = new ObjectMapper().readValue(base64, Header.class);
            return h;
        }
    }
    public static class Payload{
        public  String iss;
        public  float nbf;
        public  VerifiableCredential vc;
        public static Payload decode(String raw) throws IOException, DecodeException {
            Payload instance = new Payload();
            byte[] payloadDecoded =android.util.Base64.decode(raw,0);
            String payloadJson = inflatePayload(payloadDecoded);

            TokenStreamFactory jf = JsonFactory.builder().build();
            JsonParser jp = jf.createParser(payloadJson.getBytes());
            while(!jp.isClosed()){
                JsonToken jt = jp.nextToken();
                if(!JsonToken.FIELD_NAME.equals(jt)) continue;
                String fn = jp.currentName();
                if(fn == null) continue;

                jt = jp.nextToken();

                if(fn.equals("iss"))
                    instance.iss = jp.getText();
                else  if(fn.equals("nbf"))
                    instance.nbf = jp.getFloatValue();
                else if(fn.equals("vc")){
                    ObjectNode node = new ObjectMapper().readTree(jp);
                    instance.vc = VerifiableCredential.decode(node);
                }


            }
            return instance;
        }
        private static String inflatePayload(byte[] payloadDecoded) throws IOException {
            InflaterInputStream inflaterInputStream =
                    new InflaterInputStream(
                            new ByteArrayInputStream(payloadDecoded),
                            new Inflater(true));
            StringBuilder sb = new StringBuilder();
            while(inflaterInputStream.available() != 0){
                byte[] buf = new byte[8 * 1024];
                int read = inflaterInputStream.read(buf);
                if(read <=0) break;
                sb.append(new String(buf,0, read ));
            }
            String payload = sb.toString();

            return payload;
        }
    }
    public  static class VerifiableCredential{
        public List<Immunization> immunization = new ArrayList<>();
        public  Patient patient;
        public  static VerifiableCredential decode(ObjectNode node) throws IOException, DecodeException {
            VerifiableCredential vc = new VerifiableCredential();

            ArrayNode entries = (ArrayNode) node
                    .get("credentialSubject").get("fhirBundle").get("entry");
            for (JsonNode entry: entries) {
                JsonNode resource = entry.get("resource");
                String resourceType = resource.get("resourceType").asText();
                if("Immunization".equals(resourceType)){
                    vc.immunization.add(Immunization.decode(resource));
                }else if("Patient".equals(resourceType)){
                    vc.patient = Patient.decode(resource);
                }else {
                    throw new DecodeException("Unknown fhir resourceTye: "+resourceType);
                }
            }
            return vc;
        }
        public static class Immunization{
            public String status;
            public String lotNumber;
            public String vaccinationCode;
            public String performer;//could be multiple performers in chain
            public String occurrenceDateTime;

            public static Immunization decode(JsonNode resource) {
                Immunization im = new Immunization();
                im.lotNumber = resource.get("lotNumber").asText();
                im.occurrenceDateTime = resource.get("occurrenceDateTime").asText();
                JsonNode performer = ((ArrayNode)resource.get("performer")).get(0);
                im.performer = performer.get("actor").get("display").asText();
                im.status = resource.get("status").asText();
                im.vaccinationCode = ((ArrayNode)resource.get("vaccineCode").get("coding")).get(0)
                        .get("code").asText();
                return im;
            }
        }
        public static class Patient{
            public String birthDate;
            public String name;
            public static Patient decode(JsonNode resource){
                Patient patient = new Patient();
                JsonNode name = ((ArrayNode) resource.get("name")).get(0);
                patient.birthDate = resource.get("birthDate").asText();
                ArrayNode given = (ArrayNode) name.get("given");
                patient.name = given.get(0).asText() +" "+ given.get(1).asText()+" " +name.get("family").asText();
                return patient;
            }
        }
    }
    public static class JsonWebKey{
        public String alg;
        public String crv;
        public String kid;
        public String kty;
        public String use;
        public String x;
        public String y;
        public String[] x5c;
        public static Map<String, JsonWebKey> Parse(String json) throws IOException {
            Map<String, JsonWebKey> map = new HashMap<>();
            JsonParser jp = JsonFactory.builder().build()
                    .createParser(json.getBytes());
            ObjectNode tree = new ObjectMapper().readTree(jp);
            ArrayNode jn = (ArrayNode) tree.get("keys");

            for(JsonNode k: jn){
               JsonWebKey j = new JsonWebKey();
               j.use = k.get("use").asText();
               j.kty = k.get("kty").asText();
               j.kid = k.get("kid").asText();
               j.crv = k.get("crv").asText();
               j.alg = k.get("alg").asText();
               j.x = k.get("x").asText();
               j.y = k.get("y").asText();
               map.put(j.kid, j);
            }
            return map;
        }

        private void from(JsonNode jn) {
            this.alg = jn.get("alg").asText();
            this.crv = jn.get("crv").asText();
            this.kid = jn.get("kid").asText();
            this.kty = jn.get("kty").asText();
            this.use = jn.get("use").asText();
            this.x = jn.get("x").asText();
            this.y = jn.get("y").asText();
            ArrayNode an = jn.withArray("x5c");
            x5c = new String[an.size()];
            for(int i = 0;i<an.size();i++){
                x5c[i] = an.get(i).asText();
            }
        }
    }
}