/*
 * Copyright 2018 Google Inc.
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

package com.google.samples;

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.LocationFilter;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;

/** Location Search */
public final class LocationSearchSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START basic_location_search]
  /** Basic location Search */
  public static void basicLocationSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted
    LocationFilter locationFilter =
        new LocationFilter().setName("Mountain View, CA").setDistanceInMiles(0.5);
    JobQuery jobQuery = new JobQuery().setLocationFilters(Arrays.asList(locationFilter));
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END basic_location_search]

  // [START keyword_location_search]
  /** Keyword location Search */
  public static void keywordLocationSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted
    LocationFilter locationFilter =
        new LocationFilter().setName("Mountain View, CA").setDistanceInMiles(0.5);
    JobQuery jobQuery =
        new JobQuery()
            .setQuery("Software Engineer")
            .setLocationFilters(Arrays.asList(locationFilter));
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END keyword_location_search]

  // [START city_location_search]
  /** City location Search */
  public static void cityLocationSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted
    LocationFilter locationFilter = new LocationFilter().setName("Mountain View, CA");
    JobQuery jobQuery = new JobQuery().setLocationFilters(Arrays.asList(locationFilter));
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END city_location_search]

  // [START multi_locations_search]
  /** Multiple locations Search */
  public static void multiLocationsSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted
    JobQuery jobQuery =
        new JobQuery()
            .setLocationFilters(
                Arrays.asList(
                    new LocationFilter().setName("Mountain View, CA"),
                    new LocationFilter().setName("Sunnyvale, CA")));
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END multi_locations_search]

  // [START broadening_location_search]
  /** Broadening location Search */
  public static void broadeningLocationsSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted
    JobQuery jobQuery =
        new JobQuery()
            .setLocationFilters(Arrays.asList(new LocationFilter().setName("Mountain View, CA")));
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setEnableBroadening(true)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END broadening_location_search]

  public static void main(String... args) throws Exception {
    basicLocationSearch();
    broadeningLocationsSearch();
    cityLocationSearch();
    keywordLocationSearch();
    multiLocationsSearch();
  }
}
