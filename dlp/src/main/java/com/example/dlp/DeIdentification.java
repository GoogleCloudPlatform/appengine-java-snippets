/*
 * Copyright 2017 Google Inc.
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

package com.example.dlp;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.io.BaseEncoding;
import com.google.privacy.dlp.v2.CharacterMaskConfig;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CryptoKey;
import com.google.privacy.dlp.v2.CryptoReplaceFfxFpeConfig;
import com.google.privacy.dlp.v2.CryptoReplaceFfxFpeConfig.FfxCommonNativeAlphabet;
import com.google.privacy.dlp.v2.CustomInfoType;
import com.google.privacy.dlp.v2.CustomInfoType.SurrogateType;
import com.google.privacy.dlp.v2.DateShiftConfig;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.FieldTransformation;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InfoTypeTransformations.InfoTypeTransformation;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.KmsWrappedCryptoKey;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.ProjectName;
import com.google.privacy.dlp.v2.RecordTransformations;
import com.google.privacy.dlp.v2.ReidentifyContentRequest;
import com.google.privacy.dlp.v2.ReidentifyContentResponse;
import com.google.privacy.dlp.v2.ReplaceWithInfoTypeConfig;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.Value;
import com.google.protobuf.ByteString;
import com.google.type.Date;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class DeIdentification {

  // [START dlp_deidentify_date_shift]
  /**
   * @param inputCsvPath The path to the CSV file to deidentify
   * @param outputCsvPath (Optional) path to the output CSV file
   * @param dateFields The list of (date) fields in the CSV file to date shift
   * @param lowerBoundDays The maximum number of days to shift a date backward
   * @param upperBoundDays The maximum number of days to shift a date forward
   * @param contextFieldId (Optional) The column to determine date shift, default : a random shift
   *     amount
   * @param wrappedKey (Optional) The encrypted ('wrapped') AES-256 key to use when shifting dates
   * @param keyName (Optional) The name of the Cloud KMS key used to encrypt ('wrap') the AES-256
   *     key
   * @param projectId ID of Google Cloud project to run the API under.
   */
  private static void deidentifyWithDateShift(
      Path inputCsvPath,
      Path outputCsvPath,
      String[] dateFields,
      int lowerBoundDays,
      int upperBoundDays,
      String contextFieldId,
      String wrappedKey,
      String keyName,
      String projectId)
      throws Exception {
    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // Set the maximum days to shift a day backward (lowerbound), forward (upperbound)
      DateShiftConfig.Builder dateShiftConfigBuilder =
          DateShiftConfig.newBuilder()
              .setLowerBoundDays(lowerBoundDays)
              .setUpperBoundDays(upperBoundDays);

      // If contextFieldId, keyName or wrappedKey is set: all three arguments must be valid
      if (contextFieldId != null && keyName != null && wrappedKey != null) {
        dateShiftConfigBuilder.setContext(FieldId.newBuilder().setName(contextFieldId).build());
        KmsWrappedCryptoKey kmsWrappedCryptoKey =
            KmsWrappedCryptoKey.newBuilder()
                .setCryptoKeyName(keyName)
                .setWrappedKey(ByteString.copyFrom(BaseEncoding.base64().decode(wrappedKey)))
                .build();
        dateShiftConfigBuilder.setCryptoKey(
            CryptoKey.newBuilder().setKmsWrapped(kmsWrappedCryptoKey).build());

      } else if (contextFieldId != null || keyName != null || wrappedKey != null) {
        throw new IllegalArgumentException(
            "You must set either ALL or NONE of {contextFieldId, keyName, wrappedKey}!");
      }

      // Read and parse the CSV file
      BufferedReader br = null;
      String line;
      List<Table.Row> rows = new ArrayList<>();
      List<FieldId> headers;

      br = new BufferedReader(new FileReader(inputCsvPath.toFile()));

      // convert csv header to FieldId
      headers =
          Arrays.stream(br.readLine().split(","))
              .map(header -> FieldId.newBuilder().setName(header).build())
              .collect(Collectors.toList());

      while ((line = br.readLine()) != null) {
        // convert csv rows to Table.Row
        rows.add(convertCsvRowToTableRow(line));
      }
      br.close();

      Table table = Table.newBuilder().addAllHeaders(headers).addAllRows(rows).build();

      List<FieldId> dateFieldIds =
          Arrays.stream(dateFields)
              .map(field -> FieldId.newBuilder().setName(field).build())
              .collect(Collectors.toList());

      DateShiftConfig dateShiftConfig = dateShiftConfigBuilder.build();

      FieldTransformation fieldTransformation =
          FieldTransformation.newBuilder()
              .addAllFields(dateFieldIds)
              .setPrimitiveTransformation(
                  PrimitiveTransformation.newBuilder().setDateShiftConfig(dateShiftConfig).build())
              .build();

      DeidentifyConfig deidentifyConfig =
          DeidentifyConfig.newBuilder()
              .setRecordTransformations(
                  RecordTransformations.newBuilder()
                      .addFieldTransformations(fieldTransformation)
                      .build())
              .build();

      ContentItem tableItem = ContentItem.newBuilder().setTable(table).build();

      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setDeidentifyConfig(deidentifyConfig)
              .setItem(tableItem)
              .build();

      // Execute the deidentification request
      DeidentifyContentResponse response = dlpServiceClient.deidentifyContent(request);

      // Write out the response as a CSV file
      List<FieldId> outputHeaderFields = response.getItem().getTable().getHeadersList();
      List<Table.Row> outputRows = response.getItem().getTable().getRowsList();

      List<String> outputHeaders =
          outputHeaderFields.stream().map(FieldId::getName).collect(Collectors.toList());

      File outputFile = outputCsvPath.toFile();
      if (!outputFile.exists()) {
        outputFile.createNewFile();
      }
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

      // write out headers
      bufferedWriter.append(String.join(",", outputHeaders) + "\n");

      // write out each row
      for (Table.Row outputRow : outputRows) {
        String row =
            outputRow
                .getValuesList()
                .stream()
                .map(value -> value.getStringValue())
                .collect(Collectors.joining(","));
        bufferedWriter.append(row + "\n");
      }

      bufferedWriter.flush();
      bufferedWriter.close();

      System.out.println("Successfully saved date-shift output to: " + outputCsvPath.getFileName());
    } catch (Exception e) {
      System.out.println("Error in deidentifyWithDateShift: " + e.getMessage());
    }
  }

  // Parse string to valid date, return null when invalid
  private static LocalDate getValidDate(String dateString) {
    try {
      return LocalDate.parse(dateString);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  // convert CSV row into Table.Row
  private static Table.Row convertCsvRowToTableRow(String row) {
    String[] values = row.split(",");
    Table.Row.Builder tableRowBuilder = Table.Row.newBuilder();
    for (String value : values) {
      LocalDate date = getValidDate(value);
      if (date != null) {
        // convert to com.google.type.Date
        Date dateValue =
            Date.newBuilder()
                .setYear(date.getYear())
                .setMonth(date.getMonthValue())
                .setDay(date.getDayOfMonth())
                .build();
        Value tableValue = Value.newBuilder().setDateValue(dateValue).build();
        tableRowBuilder.addValues(tableValue);
      } else {
        tableRowBuilder.addValues(Value.newBuilder().setStringValue(value).build());
      }
    }
    return tableRowBuilder.build();
  }
  // [END dlp_deidentify_date_shift]

  /**
   * Command line application to de-identify data using the Data Loss Prevention API. Supported data
   * format: strings
   */
  public static void main(String[] args) throws Exception {

    OptionGroup optionsGroup = new OptionGroup();
    optionsGroup.setRequired(true);

    Option deidentifyReplaceWithInfoTypeOption =
        new Option("it", "info_type_replace", true, "Deidentify by replacing with info type.");
    optionsGroup.addOption(deidentifyReplaceWithInfoTypeOption);

    Option deidentifyMaskingOption =
        new Option("m", "mask", true, "Deidentify with character masking.");
    optionsGroup.addOption(deidentifyMaskingOption);

    Option deidentifyFpeOption =
        new Option("f", "fpe", true, "Deidentify with format-preserving encryption.");
    optionsGroup.addOption(deidentifyFpeOption);

    Option reidentifyFpeOption =
        new Option("r", "reid", true, "Reidentify with format-preserving encryption.");
    optionsGroup.addOption(reidentifyFpeOption);

    Option deidentifyDateShiftOption =
        new Option("d", "date", false, "Deidentify dates in a CSV file.");
    optionsGroup.addOption(deidentifyDateShiftOption);

    Options commandLineOptions = new Options();
    commandLineOptions.addOptionGroup(optionsGroup);

    Option infoTypesOption = Option.builder("infoTypes").hasArg(true).required(false).build();
    infoTypesOption.setArgs(Option.UNLIMITED_VALUES);
    commandLineOptions.addOption(infoTypesOption);

    Option maskingCharacterOption =
        Option.builder("maskingCharacter").hasArg(true).required(false).build();
    commandLineOptions.addOption(maskingCharacterOption);

    Option surrogateTypeOption =
        Option.builder("surrogateType").hasArg(true).required(false).build();
    commandLineOptions.addOption(surrogateTypeOption);

    Option numberToMaskOption = Option.builder("numberToMask").hasArg(true).required(false).build();
    commandLineOptions.addOption(numberToMaskOption);

    Option alphabetOption = Option.builder("commonAlphabet").hasArg(true).required(false).build();
    commandLineOptions.addOption(alphabetOption);

    Option wrappedKeyOption = Option.builder("wrappedKey").hasArg(true).required(false).build();
    commandLineOptions.addOption(wrappedKeyOption);

    Option keyNameOption = Option.builder("keyName").hasArg(true).required(false).build();
    commandLineOptions.addOption(keyNameOption);

    Option inputCsvPathOption = Option.builder("inputCsvPath").hasArg(true).required(false).build();
    commandLineOptions.addOption(inputCsvPathOption);

    Option outputCsvPathOption =
        Option.builder("outputCsvPath").hasArg(true).required(false).build();
    commandLineOptions.addOption(outputCsvPathOption);

    Option dateFieldsOption = Option.builder("dateFields").hasArg(true).required(false).build();
    commandLineOptions.addOption(dateFieldsOption);

    Option lowerBoundDaysOption =
        Option.builder("lowerBoundDays").hasArg(true).required(false).build();
    commandLineOptions.addOption(lowerBoundDaysOption);

    Option upperBoundDaysOption =
        Option.builder("upperBoundDays").hasArg(true).required(false).build();
    commandLineOptions.addOption(upperBoundDaysOption);

    Option contextFieldNameOption =
        Option.builder("contextField").hasArg(true).required(false).build();
    commandLineOptions.addOption(contextFieldNameOption);

    Option projectIdOption = Option.builder("projectId").hasArg(true).required(false).build();
    commandLineOptions.addOption(projectIdOption);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(commandLineOptions, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp(DeIdentification.class.getName(), commandLineOptions);
      System.exit(1);
      return;
    }

    // default to auto-detected project id when not explicitly provided
    String projectId =
        cmd.getOptionValue(projectIdOption.getOpt());

    List<InfoType> infoTypesList = Collections.emptyList();
    if (cmd.hasOption(infoTypesOption.getOpt())) {
      infoTypesList = new ArrayList<>();
      String[] infoTypes = cmd.getOptionValues(infoTypesOption.getOpt());
      for (String infoType : infoTypes) {
        infoTypesList.add(InfoType.newBuilder().setName(infoType).build());
      }
    }

  if (cmd.hasOption("d")) {
      //deidentify with date shift
      String inputCsv = cmd.getOptionValue(inputCsvPathOption.getOpt());
      String outputCsv = cmd.getOptionValue(outputCsvPathOption.getOpt());

      String contextField = cmd.getOptionValue(contextFieldNameOption.getOpt(), null);
      String wrappedKey = cmd.getOptionValue(wrappedKeyOption.getOpt(), null);
      String keyName = cmd.getOptionValue(keyNameOption.getOpt(), null);

      String[] dateFields = cmd.getOptionValue(dateFieldsOption.getOpt(), "").split(",");

      int lowerBoundsDay = Integer.valueOf(cmd.getOptionValue(lowerBoundDaysOption.getOpt()));
      int upperBoundsDay = Integer.valueOf(cmd.getOptionValue(upperBoundDaysOption.getOpt()));

      deidentifyWithDateShift(
          Paths.get(inputCsv),
          Paths.get(outputCsv),
          dateFields,
          lowerBoundsDay,
          upperBoundsDay,
          contextField,
          wrappedKey,
          keyName,
          projectId);
    }
  }
}
