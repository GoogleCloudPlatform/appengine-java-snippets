package compute.reservation;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.cloud.compute.v1.Reservation;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 300, unit = TimeUnit.SECONDS)
public class GetComputeReservationTest {

  private static String PROJECT_ID;
  private static String ZONE;
  private static String RESERVATION_NAME;

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    ZONE = "us-central1-a";
    RESERVATION_NAME = "test-reservation-" + UUID.randomUUID();

    // Create the reservation.
    Reservation reservation = CreateReservation
        .createReservation(
            PROJECT_ID,
            ZONE,
            RESERVATION_NAME);

    assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
  }

  @AfterAll
  public static void tearDown()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
  }

  @Test
  public void testGetReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Reservation reservation = GetComputeReservation.getReservation(
        PROJECT_ID,
        RESERVATION_NAME,
        ZONE);

    assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
  }
}