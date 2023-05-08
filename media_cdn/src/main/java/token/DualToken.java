/*
 *  Copyright 2023 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package token;

// [START mediacdn_dualtoken_sign_token]

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.util.encoders.Hex;

public class DualToken {

  public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
    // TODO(developer): Replace these variables before running the sample.
    byte[] base64Key = new byte[]{};
    String signatureAlgorithm = "ed25519";
    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    Instant expiresTime = Instant.from(formatter.parse("2022-09-13T12:00:00Z"));
    String fullPath = "http://10.20.30.40/";

    DualToken.signToken(base64Key,
        signatureAlgorithm,
        null,
        expiresTime,
        fullPath, "", "", "", "", new ArrayList<>(), "");
  }

  // Gets the Signed URL Suffix string for the Media CDN' Short token URL requests.
  // One of (`urlPrefix`, `fullPath`, `pathGlobs`) must be included in each input.
  // Args:
  //     base64Key: Secret key as a base64 encoded string.
  //     signatureAlgorithm: Algorithm can be either `SHA1` or `SHA256` or `Ed25519`.
  //     startTime: Start time as a UTC datetime object.
  //     expirationTime: Expiration time as a UTC datetime object. If None, an expiration time 1 hour from now will be used.
  //     urlPrefix: the URL prefix to sign, including protocol.
  //                 For example: http://example.com/path/ for URLs under /path or http://example.com/path?param=1
  //     fullPath:  A full path to sign, starting with the first '/'.
  //                 For example: /path/to/content.mp4
  //     pathGlobs: a set of ','- or '!'-delimited path glob strings.
  //                 For example: /tv/*!/film/* to sign paths starting with /tv/ or /film/ in any URL.
  //     sessionId: a unique identifier for the session
  //     data: data payload to include in the token
  //     headers: header name and value to include in the signed token in name=value format.  May be specified more than once.
  //                 For example: [{'name': 'foo', 'value': 'bar'}, {'name': 'baz', 'value': 'qux'}]
  //     ipRanges: A list of comma separated ip ranges. Both IPv4 and IPv6 ranges are acceptable.
  //                 For example: "203.0.113.0/24,2001:db8:4a7f:a732/64"
  // Result:
  //     The Signed URL appended with the query parameters based on the
  //     specified URL prefix and configuration.
  public static void signToken(byte[] base64Key, String signatureAlgorithm, Instant startTime,
      Instant expirationTime, String urlPrefix, String fullPath,
      String pathGlobs, String sessionId, String data, List<Header> headers, String ipRanges)
      throws NoSuchAlgorithmException, InvalidKeyException {

    String field = "";
    byte[] decodedKey = Base64.getUrlDecoder().decode(base64Key);
    String algorithm = signatureAlgorithm.toLowerCase();

    // For most fields, the value we put in the token and the value we must sign
    // are the same.  The FullPath and Headers use a different string for the
    // value to be signed compared to the token.  To illustrate this difference,
    // we'll keep the token and the value to be signed separate.

    List<String> tokens = new ArrayList<>();
    List<String> toSign = new ArrayList<>();

    // Check for `fullPath` or `pathGlobs` or `urlPrefix`.
    if (!fullPath.isEmpty()) {
      tokens.add("FullPath");
      toSign.add(String.format("FullPath=%s", fullPath));
    } else if (!pathGlobs.isEmpty()) {
      pathGlobs = pathGlobs.trim();
      field = String.format("PathGlobs=%s", pathGlobs);
      tokens.add(field);
      toSign.add(field);
    } else if (!urlPrefix.isEmpty()) {
      field = String.format("URLPrefix=%s",
          base64Encoder(urlPrefix.getBytes(StandardCharsets.UTF_8)));
      tokens.add(field);
      toSign.add(field);
    } else {
      throw new IllegalArgumentException(
          "User Input Missing: One of `urlPrefix`, `fullPath` or `pathGlobs` must be specified");
    }

    // Check & parse optional params.
    long epochDuration;
    if (startTime != null) {
      epochDuration = ChronoUnit.SECONDS.between(Instant.EPOCH, startTime);
      field = String.format("Starts=%s", epochDuration);
      tokens.add(field);
      toSign.add(field);
    }

    if (expirationTime == null) {
      expirationTime = Instant.now().plus(1, ChronoUnit.HOURS);
    }
    epochDuration = ChronoUnit.SECONDS.between(Instant.EPOCH, expirationTime);
    field = String.format("Expires=%s", epochDuration);
    tokens.add(field);
    toSign.add(field);

    if (!sessionId.isEmpty()) {
      field = String.format("SessionID=%s", sessionId);
      tokens.add(field);
      toSign.add(field);
    }

    if (!data.isEmpty()) {
      field = String.format("Data=%s", data);
      tokens.add(field);
      toSign.add(field);
    }

    if (!headers.isEmpty()) {
      List<String> headerNames = new ArrayList<>();
      List<String> headerPairs = new ArrayList<>();

      for (Header entry : headers) {
        headerNames.add(entry.getName());
        headerPairs.add(String.format("%s=%s", entry.getName(), entry.getValue()));
      }
      tokens.add(String.format("Headers=%s", String.join(",", headerNames)));
      toSign.add(String.format("Headers=%s", String.join(",", headerPairs)));
    }

    if (!ipRanges.isEmpty()) {
      field = String.format("IPRanges=%s",
          base64Encoder(ipRanges.getBytes(StandardCharsets.US_ASCII)));
      tokens.add(field);
      toSign.add(field);
    }

    // Generating token.
    String toSignJoined = String.join("~", toSign);
    byte[] toSignBytes = toSignJoined.getBytes(StandardCharsets.UTF_8);

    if (algorithm.equalsIgnoreCase("ed25519")) {
      Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(decodedKey, 0);
      Ed25519Signer signer = new Ed25519Signer();
      signer.init(true, privateKey);
      signer.update(toSignBytes, 0, toSignBytes.length);
      byte[] signature = signer.generateSignature();
      tokens.add(String.format("Signature=%s", base64Encoder(signature)));
    }
    else if (algorithm.equalsIgnoreCase("sha256")) {
      String sha256 = "HmacSHA256";
      Mac mac = Mac.getInstance(sha256);
      SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, sha256);
      mac.init(secretKeySpec);
      byte[] signature = mac.doFinal(toSignBytes);
      tokens.add(String.format("hmac=%s", Hex.toHexString(signature)));
    }
    else if (algorithm.equalsIgnoreCase("sha1")) {
      String sha1 = "HmacSHA1";
      Mac mac = Mac.getInstance(sha1);
      SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, sha1);
      mac.init(secretKeySpec);
      byte[] signature = mac.doFinal(toSignBytes);
      tokens.add(String.format("hmac=%s", Hex.toHexString(signature)));
    }
    else {
      throw new Error(
          "Input Missing Error: `signatureAlgorithm` can only be one of `sha1`, `sha256` or `ed25519`");
    }
    // The Signed URL appended with the query parameters based on the
    // specified URL prefix and configuration.
    System.out.println(String.join("~", tokens));
  }

  // Returns a base64-encoded string compatible with Media CDN.
  // Media CDN uses URL-safe base64 encoding and strips off the padding at the
  // end.
  public static String base64Encoder(byte[] value) {
    byte[] encodedBytes = Base64.getUrlEncoder().withoutPadding().encode(value);
    return new String(encodedBytes, StandardCharsets.UTF_8);
  }

  public static class Header {

    private String name;
    private String value;

    public Header(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return "Header{" +
          "name='" + name + '\'' +
          ", value='" + value + '\'' +
          '}';
    }
  }

}
// [END mediacdn_dualtoken_sign_token]