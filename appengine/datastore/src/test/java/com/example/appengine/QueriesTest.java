/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine;

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests to demonstrate App Engine Datastore queries.
 */
@RunWith(JUnit4.class)
public class QueriesTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void propertyFilterExample_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity p1 = new Entity("Person");
    p1.setProperty("height", 120);
    Entity p2 = new Entity("Person");
    p2.setProperty("height", 180);
    Entity p3 = new Entity("Person");
    p3.setProperty("height", 160);
    datastore.put(ImmutableList.<Entity>of(p1, p2, p3));

    // Act
    long minHeight = 160;
    // [START property_filter_example]
    Filter propertyFilter =
        new FilterPredicate("height", FilterOperator.GREATER_THAN_OR_EQUAL, minHeight);
    Query q = new Query("Person").setFilter(propertyFilter);
    // [END property_filter_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(p2.getKey(), p3.getKey());
  }

  @Test
  public void keyFilterExample_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    Entity b = new Entity("Person", "b");
    Entity c = new Entity("Person", "c");
    Entity aa = new Entity("Person", "aa", b.getKey());
    Entity bb = new Entity("Person", "bb", b.getKey());
    Entity aaa = new Entity("Person", "aaa", bb.getKey());
    Entity bbb = new Entity("Person", "bbb", bb.getKey());
    datastore.put(ImmutableList.<Entity>of(a, b, c, aa, bb, aaa, bbb));

    // Act
    Key lastSeenKey = bb.getKey();
    // [START key_filter_example]
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, lastSeenKey);
    Query q = new Query("Person").setFilter(keyFilter);
    // [END key_filter_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(
            // Ancestor path "b/bb/aaa" is greater than "b/bb".
            aaa.getKey(),
            // Ancestor path "b/bb/bbb" is greater than "b/bb".
            bbb.getKey(),
            // Key name identifier "c" is greater than b.
            c.getKey());
  }

  @Test
  public void keyFilterExample_kindless_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Child", "a");
    Entity b = new Entity("Child", "b");
    Entity c = new Entity("Child", "c");
    Entity aa = new Entity("Child", "aa", b.getKey());
    Entity bb = new Entity("Child", "bb", b.getKey());
    Entity aaa = new Entity("Child", "aaa", bb.getKey());
    Entity bbb = new Entity("Child", "bbb", bb.getKey());
    Entity adult = new Entity("Adult", "a");
    Entity zooAnimal = new Entity("ZooAnimal", "a");
    datastore.put(ImmutableList.<Entity>of(a, b, c, aa, bb, aaa, bbb, adult, zooAnimal));

    // Act
    Key lastSeenKey = bb.getKey();
    // [START kindless_query_example]
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, lastSeenKey);
    Query q = new Query().setFilter(keyFilter);
    // [END kindless_query_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(
            // Ancestor path "b/bb/aaa" is greater than "b/bb".
            aaa.getKey(),
            // Ancestor path "b/bb/bbb" is greater than "b/bb".
            bbb.getKey(),
            // Kind "ZooAnimal" is greater than "Child"
            zooAnimal.getKey(),
            // Key name identifier "c" is greater than b.
            c.getKey());
  }

  @Test
  public void ancestorFilterExample_returnsMatchingEntities() throws Exception {
    Entity a = new Entity("Person", "a");
    Entity b = new Entity("Person", "b");
    Entity aa = new Entity("Person", "aa", a.getKey());
    Entity ab = new Entity("Person", "ab", a.getKey());
    Entity bb = new Entity("Person", "bb", b.getKey());
    datastore.put(ImmutableList.<Entity>of(a, b, aa, ab, bb));

    Key ancestorKey = a.getKey();
    // [START ancestor_filter_example]
    Query q = new Query("Person").setAncestor(ancestorKey);
    // [END ancestor_filter_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(a.getKey(), aa.getKey(), ab.getKey());
  }

  @Test
  public void ancestorQueryExample_returnsMatchingEntities() throws Exception {
    // [START ancestor_query_example]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity tom = new Entity("Person", "Tom");
    Key tomKey = tom.getKey();
    datastore.put(tom);

    Entity weddingPhoto = new Entity("Photo", tomKey);
    weddingPhoto.setProperty("imageURL", "http://domain.com/some/path/to/wedding_photo.jpg");

    Entity babyPhoto = new Entity("Photo", tomKey);
    babyPhoto.setProperty("imageURL", "http://domain.com/some/path/to/baby_photo.jpg");

    Entity dancePhoto = new Entity("Photo", tomKey);
    dancePhoto.setProperty("imageURL", "http://domain.com/some/path/to/dance_photo.jpg");

    Entity campingPhoto = new Entity("Photo");
    campingPhoto.setProperty("imageURL", "http://domain.com/some/path/to/camping_photo.jpg");

    List<Entity> photoList = Arrays.asList(weddingPhoto, babyPhoto, dancePhoto, campingPhoto);
    datastore.put(photoList);

    Query photoQuery = new Query("Photo").setAncestor(tomKey);

    // This returns weddingPhoto, babyPhoto, and dancePhoto,
    // but not campingPhoto, because tom is not an ancestor
    List<Entity> results =
        datastore.prepare(photoQuery).asList(FetchOptions.Builder.withDefaults());
    // [END ancestor_query_example]

    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(weddingPhoto.getKey(), babyPhoto.getKey(), dancePhoto.getKey());
  }

  @Test
  public void ancestorQueryExample_kindlessKeyFilter_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Grandparent", "a");
    Entity b = new Entity("Grandparent", "b");
    Entity c = new Entity("Grandparent", "c");
    Entity aa = new Entity("Parent", "aa", a.getKey());
    Entity ba = new Entity("Parent", "ba", b.getKey());
    Entity bb = new Entity("Parent", "bb", b.getKey());
    Entity bc = new Entity("Parent", "bc", b.getKey());
    Entity cc = new Entity("Parent", "cc", c.getKey());
    Entity aaa = new Entity("Child", "aaa", aa.getKey());
    Entity bbb = new Entity("Child", "bbb", bb.getKey());
    datastore.put(ImmutableList.<Entity>of(a, b, c, aa, ba, bb, bc, cc, aaa, bbb));

    // Act
    Key ancestorKey = b.getKey();
    Key lastSeenKey = bb.getKey();
    // [START kindless_ancestor_key_query_example]
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, lastSeenKey);
    Query q = new Query().setAncestor(ancestorKey).setFilter(keyFilter);
    // [END kindless_ancestor_key_query_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(bc.getKey(), bbb.getKey());
  }

  @Test
  public void ancestorQueryExample_kindlessKeyFilterFull_returnsMatchingEntities()
      throws Exception {
    // [START kindless_ancestor_query_example]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity tom = new Entity("Person", "Tom");
    Key tomKey = tom.getKey();
    datastore.put(tom);

    Entity weddingPhoto = new Entity("Photo", tomKey);
    weddingPhoto.setProperty("imageURL", "http://domain.com/some/path/to/wedding_photo.jpg");

    Entity weddingVideo = new Entity("Video", tomKey);
    weddingVideo.setProperty("videoURL", "http://domain.com/some/path/to/wedding_video.avi");

    List<Entity> mediaList = Arrays.asList(weddingPhoto, weddingVideo);
    datastore.put(mediaList);

    // By default, ancestor queries include the specified ancestor itself.
    // The following filter excludes the ancestor from the query results.
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, tomKey);

    Query mediaQuery = new Query().setAncestor(tomKey).setFilter(keyFilter);

    // Returns both weddingPhoto and weddingVideo,
    // even though they are of different entity kinds
    List<Entity> results =
        datastore.prepare(mediaQuery).asList(FetchOptions.Builder.withDefaults());
    // [END kindless_ancestor_query_example]

    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(weddingPhoto.getKey(), weddingVideo.getKey());
  }

  @Test
  public void keysOnlyExample_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    Entity b = new Entity("Building", "b");
    Entity c = new Entity("Person", "c");
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // [START keys_only_example]
    Query q = new Query("Person").setKeysOnly();
    // [END keys_only_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results)).named("query result keys").containsExactly(a.getKey(), c.getKey());
  }

  @Test
  public void sortOrderExample_returnsSortedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("lastName", "Alpha");
    a.setProperty("height", 100);
    Entity b = new Entity("Person", "b");
    b.setProperty("lastName", "Bravo");
    b.setProperty("height", 200);
    Entity c = new Entity("Person", "c");
    c.setProperty("lastName", "Charlie");
    c.setProperty("height", 300);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // Act
    // [START sort_order_example]
    // Order alphabetically by last name:
    Query q1 = new Query("Person").addSort("lastName", SortDirection.ASCENDING);

    // Order by height, tallest to shortest:
    Query q2 = new Query("Person").addSort("height", SortDirection.DESCENDING);
    // [END sort_order_example]

    // Assert
    List<Entity> lastNameResults =
        datastore.prepare(q1).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(lastNameResults))
        .named("last name query result keys")
        .containsExactly(a.getKey(), b.getKey(), c.getKey())
        .inOrder();
    List<Entity> heightResults = datastore.prepare(q2).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(heightResults))
        .named("height query result keys")
        .containsExactly(c.getKey(), b.getKey(), a.getKey())
        .inOrder();
  }

  @Test
  public void sortOrderExample_multipleSortOrders_returnsSortedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("lastName", "Alpha");
    a.setProperty("height", 100);
    Entity b1 = new Entity("Person", "b1");
    b1.setProperty("lastName", "Bravo");
    b1.setProperty("height", 150);
    Entity b2 = new Entity("Person", "b2");
    b2.setProperty("lastName", "Bravo");
    b2.setProperty("height", 200);
    Entity c = new Entity("Person", "c");
    c.setProperty("lastName", "Charlie");
    c.setProperty("height", 300);
    datastore.put(ImmutableList.<Entity>of(a, b1, b2, c));

    // Act
    // [START multiple_sort_orders_example]
    Query q =
        new Query("Person")
            .addSort("lastName", SortDirection.ASCENDING)
            .addSort("height", SortDirection.DESCENDING);
    // [END multiple_sort_orders_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(a.getKey(), b2.getKey(), b1.getKey(), c.getKey())
        .inOrder();
  }

  @Test
  public void queryInterface_multipleFilters_printsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("firstName", "Alph");
    a.setProperty("lastName", "Alpha");
    a.setProperty("height", 60);
    Entity b = new Entity("Person", "b");
    b.setProperty("firstName", "Bee");
    b.setProperty("lastName", "Bravo");
    b.setProperty("height", 70);
    Entity c = new Entity("Person", "c");
    c.setProperty("firstName", "Charles");
    c.setProperty("lastName", "Charlie");
    c.setProperty("height", 100);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    StringWriter buf = new StringWriter();
    PrintWriter out = new PrintWriter(buf);
    long minHeight = 60;
    long maxHeight = 72;

    // Act
    // [START interface_1]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Filter heightMinFilter =
        new FilterPredicate("height", FilterOperator.GREATER_THAN_OR_EQUAL, minHeight);

    Filter heightMaxFilter =
        new FilterPredicate("height", FilterOperator.LESS_THAN_OR_EQUAL, maxHeight);

    // Use CompositeFilter to combine multiple filters
    CompositeFilter heightRangeFilter =
        CompositeFilterOperator.and(heightMinFilter, heightMaxFilter);

    // Use class Query to assemble a query
    Query q = new Query("Person").setFilter(heightRangeFilter);

    // Use PreparedQuery interface to retrieve results
    PreparedQuery pq = datastore.prepare(q);

    for (Entity result : pq.asIterable()) {
      String firstName = (String) result.getProperty("firstName");
      String lastName = (String) result.getProperty("lastName");
      Long height = (Long) result.getProperty("height");

      out.println(firstName + " " + lastName + ", " + height + " inches tall");
    }
    // [END interface_1]

    // Assert
    assertThat(buf.toString()).contains("Alph Alpha, 60 inches tall");
    assertThat(buf.toString()).contains("Bee Bravo, 70 inches tall");
    assertThat(buf.toString()).doesNotContain("Charlie");
  }

  @Test
  public void queryInterface_singleFilter_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("height", 100);
    Entity b = new Entity("Person", "b");
    b.setProperty("height", 150);
    Entity c = new Entity("Person", "c");
    c.setProperty("height", 300);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // Act
    long minHeight = 150;
    // [START interface_2]
    Filter heightMinFilter =
        new FilterPredicate("height", FilterOperator.GREATER_THAN_OR_EQUAL, minHeight);

    Query q = new Query("Person").setFilter(heightMinFilter);
    // [END interface_2]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results)).named("query result keys").containsExactly(b.getKey(), c.getKey());
  }

  @Test
  public void queryInterface_orFilter_printsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("height", 100);
    Entity b = new Entity("Person", "b");
    b.setProperty("height", 150);
    Entity c = new Entity("Person", "c");
    c.setProperty("height", 200);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    StringWriter buf = new StringWriter();
    PrintWriter out = new PrintWriter(buf);
    long minHeight = 125;
    long maxHeight = 175;

    // Act
    // [START interface_3]
    Filter tooShortFilter = new FilterPredicate("height", FilterOperator.LESS_THAN, minHeight);

    Filter tooTallFilter = new FilterPredicate("height", FilterOperator.GREATER_THAN, maxHeight);

    Filter heightOutOfRangeFilter = CompositeFilterOperator.or(tooShortFilter, tooTallFilter);

    Query q = new Query("Person").setFilter(heightOutOfRangeFilter);
    // [END interface_3]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results)).named("query result keys").containsExactly(a.getKey(), c.getKey());
  }

  @Test
  public void queryRestrictions_compositeFilter_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("birthYear", 1930);
    Entity b = new Entity("Person", "b");
    b.setProperty("birthYear", 1960);
    Entity c = new Entity("Person", "c");
    c.setProperty("birthYear", 1990);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // Act
    long minBirthYear = 1940;
    long maxBirthYear = 1980;
    // [START inequality_filters_one_property_valid_example_1]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Filter birthYearMaxFilter =
        new FilterPredicate("birthYear", FilterOperator.LESS_THAN_OR_EQUAL, maxBirthYear);

    Filter birthYearRangeFilter =
        CompositeFilterOperator.and(birthYearMinFilter, birthYearMaxFilter);

    Query q = new Query("Person").setFilter(birthYearRangeFilter);
    // [END inequality_filters_one_property_valid_example_1]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results)).named("query result keys").containsExactly(b.getKey());
  }

  @Test
  public void queryRestrictions_compositeFilter_isInvalid() throws Exception {
    long minBirthYear = 1940;
    long maxHeight = 200;
    // [START inequality_filters_one_property_invalid_example]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Filter heightMaxFilter =
        new FilterPredicate("height", FilterOperator.LESS_THAN_OR_EQUAL, maxHeight);

    Filter invalidFilter = CompositeFilterOperator.and(birthYearMinFilter, heightMaxFilter);

    Query q = new Query("Person").setFilter(invalidFilter);
    // [END inequality_filters_one_property_invalid_example]

    // Note: The local devserver behavior is different than the production
    // version of Cloud Datastore, so there aren't any assertions we can make
    // in this test.  The query appears to work with the local test runner,
    // but will fail in production.
  }

  @Test
  public void queryRestrictions_compositeEqualFilter_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("birthYear", 1930);
    a.setProperty("city", "Somewhere");
    a.setProperty("lastName", "Someone");
    Entity b = new Entity("Person", "b");
    b.setProperty("birthYear", 1960);
    b.setProperty("city", "Somewhere");
    b.setProperty("lastName", "Someone");
    Entity c = new Entity("Person", "c");
    c.setProperty("birthYear", 1990);
    c.setProperty("city", "Somewhere");
    c.setProperty("lastName", "Someone");
    Entity d = new Entity("Person", "d");
    d.setProperty("birthYear", 1960);
    d.setProperty("city", "Nowhere");
    d.setProperty("lastName", "Someone");
    Entity e = new Entity("Person", "e");
    e.setProperty("birthYear", 1960);
    e.setProperty("city", "Somewhere");
    e.setProperty("lastName", "Noone");
    datastore.put(ImmutableList.<Entity>of(a, b, c, d, e));
    long minBirthYear = 1940;
    long maxBirthYear = 1980;
    String targetCity = "Somewhere";
    String targetLastName = "Someone";

    // [START inequality_filters_one_property_valid_example_2]
    Filter lastNameFilter = new FilterPredicate("lastName", FilterOperator.EQUAL, targetLastName);

    Filter cityFilter = new FilterPredicate("city", FilterOperator.EQUAL, targetCity);

    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Filter birthYearMaxFilter =
        new FilterPredicate("birthYear", FilterOperator.LESS_THAN_OR_EQUAL, maxBirthYear);

    Filter validFilter =
        CompositeFilterOperator.and(
            lastNameFilter, cityFilter, birthYearMinFilter, birthYearMaxFilter);

    Query q = new Query("Person").setFilter(validFilter);
    // [END inequality_filters_one_property_valid_example_2]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results)).named("query result keys").containsExactly(b.getKey());
  }

  @Test
  public void queryRestrictions_inequalitySortedFirst_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("birthYear", 1930);
    a.setProperty("lastName", "Someone");
    Entity b = new Entity("Person", "b");
    b.setProperty("birthYear", 1990);
    b.setProperty("lastName", "Bravo");
    Entity c = new Entity("Person", "c");
    c.setProperty("birthYear", 1960);
    c.setProperty("lastName", "Charlie");
    Entity d = new Entity("Person", "d");
    d.setProperty("birthYear", 1960);
    d.setProperty("lastName", "Delta");
    datastore.put(ImmutableList.<Entity>of(a, b, c, d));
    long minBirthYear = 1940;

    // [START inequality_filters_sort_orders_valid_example]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Query q =
        new Query("Person")
            .setFilter(birthYearMinFilter)
            .addSort("birthYear", SortDirection.ASCENDING)
            .addSort("lastName", SortDirection.ASCENDING);
    // [END inequality_filters_sort_orders_valid_example]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(c.getKey(), d.getKey(), b.getKey())
        .inOrder();
  }

  @Test
  public void queryRestrictions_missingSortOnInequality_isInvalid() throws Exception {
    long minBirthYear = 1940;
    // [START inequality_filters_sort_orders_invalid_example_1]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    // Not valid. Missing sort on birthYear.
    Query q =
        new Query("Person")
            .setFilter(birthYearMinFilter)
            .addSort("lastName", SortDirection.ASCENDING);
    // [END inequality_filters_sort_orders_invalid_example_1]

    // Note: The local devserver behavior is different than the production
    // version of Cloud Datastore, so there aren't any assertions we can make
    // in this test.  The query appears to work with the local test runner,
    // but will fail in production.
  }

  @Test
  public void queryRestrictions_sortWrongOrderOnInequality_isInvalid() throws Exception {
    long minBirthYear = 1940;
    // [START inequality_filters_sort_orders_invalid_example_2]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    // Not valid. Sort on birthYear needs to be first.
    Query q =
        new Query("Person")
            .setFilter(birthYearMinFilter)
            .addSort("lastName", SortDirection.ASCENDING)
            .addSort("birthYear", SortDirection.ASCENDING);
    // [END inequality_filters_sort_orders_invalid_example_2]

    // Note: The local devserver behavior is different than the production
    // version of Cloud Datastore, so there aren't any assertions we can make
    // in this test.  The query appears to work with the local test runner,
    // but will fail in production.
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesAllMustMatch_returnsNoEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    ArrayList<Long> xs = new ArrayList<>();
    xs.add(1L);
    xs.add(2L);
    a.setProperty("x", xs);
    datastore.put(a);

    // [START surprising_behavior_example_1]
    Query q =
        new Query("Widget")
            .setFilter(new FilterPredicate("x", FilterOperator.GREATER_THAN, 1))
            .setFilter(new FilterPredicate("x", FilterOperator.LESS_THAN, 2));
    // [END surprising_behavior_example_1]

    // Note: The documentation describes that the entity "a" will not match
    // because no value matches all filters.  When run with the local test
    // runner, the entity "a" *is* matched.  This may be a difference in
    // behavior between the local devserver and Cloud Datastore, so there
    // aren't any assertions we can make in this test.
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesEquals_returnsMatchedEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    a.setProperty("x", ImmutableList.<Long>of(1L, 2L));
    Entity b = new Entity("Widget", "b");
    b.setProperty("x", ImmutableList.<Long>of(1L, 3L));
    Entity c = new Entity("Widget", "c");
    c.setProperty("x", ImmutableList.<Long>of(-6L, 2L));
    Entity d = new Entity("Widget", "d");
    d.setProperty("x", ImmutableList.<Long>of(-6L, 4L));
    datastore.put(ImmutableList.<Entity>of(a, b, c, d));

    // [START surprising_behavior_example_2]
    Query q =
        new Query("Widget")
            .setFilter(new FilterPredicate("x", FilterOperator.EQUAL, 1))
            .setFilter(new FilterPredicate("x", FilterOperator.EQUAL, 2));
    // [END surprising_behavior_example_2]

    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results)).named("query result keys").contains(a.getKey());

    // Note: When run in the test server, this matches "c" as expected and does
    // not match "d" as expected.  For some reason it does *not* match "b".
    // The behavior of queries on repeated values is definitely surprising.
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesNotEquals_returnsMatchedEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    a.setProperty("x", ImmutableList.<Long>of(1L, 2L));
    Entity b = new Entity("Widget", "b");
    b.setProperty("x", ImmutableList.<Long>of(1L, 3L));
    Entity c = new Entity("Widget", "c");
    c.setProperty("x", ImmutableList.<Long>of(-6L, 2L));
    Entity d = new Entity("Widget", "d");
    d.setProperty("x", ImmutableList.<Long>of(-6L, 4L));
    Entity e = new Entity("Widget", "e");
    e.setProperty("x", ImmutableList.<Long>of(1L));
    datastore.put(ImmutableList.<Entity>of(a, b, c, d, e));

    // [START surprising_behavior_example_3]
    Query q = new Query("Widget").setFilter(new FilterPredicate("x", FilterOperator.NOT_EQUAL, 1));
    // [END surprising_behavior_example_3]

    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results))
        .named("query result keys")
        .containsExactly(a.getKey(), b.getKey(), c.getKey(), d.getKey());
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesTwoNotEquals_returnsMatchedEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    a.setProperty("x", ImmutableList.<Long>of(1L, 2L));
    Entity b = new Entity("Widget", "b");
    b.setProperty("x", ImmutableList.<Long>of(1L, 2L, 3L));
    datastore.put(ImmutableList.<Entity>of(a, b));

    // [START surprising_behavior_example_4]
    Query q =
        new Query("Widget")
            .setFilter(new FilterPredicate("x", FilterOperator.NOT_EQUAL, 1))
            .setFilter(new FilterPredicate("x", FilterOperator.NOT_EQUAL, 2));
    // [END surprising_behavior_example_4]

    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(getKeys(results)).named("query result keys").contains(b.getKey());

    // Note: The documentation describes that the entity "a" will not match.
    // When run with the local test runner, the entity "a" *is* matched.  This
    // may be a difference in behavior between the local devserver and Cloud
    // Datastore.
  }

  private ImmutableList<Key> getKeys(List<Entity> entities) {
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : entities) {
      keys.add(entity.getKey());
    }
    return keys.build();
  }
}
