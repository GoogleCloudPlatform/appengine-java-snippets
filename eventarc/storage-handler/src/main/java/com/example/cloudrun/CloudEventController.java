package com.example.cloudrun;

// [START eventarc_storage_cloudevent_handler]

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.util.JsonFormat;

import io.cloudevents.CloudEvent;

@RestController
public class CloudEventController {

	@RequestMapping(value = "/", method = RequestMethod.POST, consumes = "application/json")
	ResponseEntity<String> handleCloudEvent(@RequestBody CloudEvent cloudEvent) throws Exception {

		// CloudEvent information
		System.out.println("Id: " + cloudEvent.getId());
		System.out.println("Source: " + cloudEvent.getSource());
		System.out.println("Type: " + cloudEvent.getType());

		String json = new String(cloudEvent.getData().toBytes());
		StorageObjectData.Builder builder = StorageObjectData.newBuilder();
		JsonFormat.parser().merge(json, builder);
		StorageObjectData data = builder.build();

		StringBuilder mb = new StringBuilder();
		mb.append(
			String.format("Cloud Storage object changed: %s/%s modified at %s\n",
			data.getBucket(), data.getName(), data.getUpdated()));

		return ResponseEntity.ok().body(mb.toString());
	}

    // Handle exceptions from CloudEvent Message Converter
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid CloudEvent received")
    public void noop(){}
}
// [END eventarc_storage_cloudevent_handler]
