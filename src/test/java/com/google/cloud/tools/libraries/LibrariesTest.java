/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.libraries;

import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LibrariesTest {

  private JsonObject[] apis;

  @Before
  public void parseJson() {
    JsonReaderFactory factory = Json.createReaderFactory(null);
    InputStream in = LibrariesTest.class.getResourceAsStream("libraries.json");
    JsonReader reader = factory.createReader(in);
    apis = reader.readArray().toArray(new JsonObject[0]);
  }

  @Test
  public void testJson() throws IOException {
    Assert.assertTrue(apis.length > 0);
    for (int i = 0; i < apis.length; i++) {
      assertApi(apis[i]);
    }
  }

  private static final String[] statuses = {"early access", "alpha", "beta", "GA", "deprecated"};

  private static void assertApi(JsonObject api) throws IOException {
    Assert.assertFalse(api.getString("name").isEmpty());
    Assert.assertFalse(api.getString("description").isEmpty());
    String transports = api.getJsonArray("transports").getString(0);
    Assert.assertTrue(
        transports + " is not a recognized transport",
        "http".equals(transports) || "grpc".equals(transports));
    assertReachable(api.getString("documentation"));
    try {
      assertReachable(api.getString("icon"));
    } catch (NullPointerException ex) {
      // no icon element to test
    }
    JsonArray clients = api.getJsonArray("clients");
    Assert.assertFalse(clients.isEmpty());
    for (int i = 0; i < clients.size(); i++) {
      JsonObject client = (JsonObject) clients.get(i);
      String launchStage = client.getString("launchStage");
      Assert.assertThat(statuses, hasItemInArray(launchStage));
      try {
        assertReachable(client.getString("site"));
      } catch (NullPointerException ex) {
        // no site element to test
      }
      assertReachable(client.getString("apireference"));
      Assert.assertTrue(client.getString("languageLevel").matches("1\\.\\d+\\.\\d+"));
      Assert.assertFalse(client.getString("name").isEmpty());
      Assert.assertNotNull(client.getJsonObject("mavenCoordinates"));
      if (client.getString("source") != null) {
        assertReachable(client.getString("source"));
      }
    }
  }

  private static void assertReachable(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    Assert.assertEquals("Could not reach " + url, 200, connection.getResponseCode());
  }

  @Test
  public void testDuplicates() {
    Map<String, String> apiCoordinates = new HashMap<>();
    for (JsonObject api : apis) {
      String name = api.getString("name");
      if (apiCoordinates.containsKey(name)) {
        Assert.fail(name + " is defined twice");
      }
      JsonObject coordinates =
          ((JsonObject) api.getJsonArray("clients").get(0)).getJsonObject("mavenCoordinates");
      String mavenCoordinates =
          coordinates.getString("groupId") + ":" + coordinates.getString("artifactId");
      if (apiCoordinates.containsValue(mavenCoordinates)) {
        Assert.fail(mavenCoordinates + " is defined twice");
      }
      apiCoordinates.put(name, mavenCoordinates);
    }
  }
}