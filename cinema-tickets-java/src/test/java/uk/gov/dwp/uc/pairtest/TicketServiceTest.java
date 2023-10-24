package uk.gov.dwp.uc.pairtest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


public class TicketServiceTest {
  private TicketPaymentService mockPaymentService;
  private SeatReservationService mockReservationService;
  private TicketService ticketService;

  @Before
  public final void setup() {
    this.mockPaymentService = mock(TicketPaymentService.class);
    this.mockReservationService = mock(SeatReservationService.class);
    this.ticketService =
        new TicketServiceImpl(this.mockPaymentService, this.mockReservationService);
  }

  @Test
  public void purchaseTickets_nullAccountId_throwsException() {
    final TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);

    final var exception = assertInvalidPurchaseException(() -> {
      this.ticketService.purchaseTickets(null, request);
    });

    assertExceptionContains(exception, "accountId must not be null.");
  }

  @Test
  public void purchaseTickets_nullTicketTypeRequests_throwsException() {
    final Long accountId = Long.valueOf(3);
    final TicketTypeRequest[] requests = null;

    final var exception = assertInvalidPurchaseException(() -> {
      this.ticketService.purchaseTickets(accountId, requests);
    });

    assertExceptionContains(exception, "ticketTypeRequests must not be null");
  }

  @Test
  public void purchaseTickets_tooFewTickets_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {};

    final var exception = assertInvalidPurchaseException(() -> {
      this.ticketService.purchaseTickets(accountId, requests);
    });

    assertExceptionContains(exception, "At least 1 adult ticket must be purchased.");
  }

  @Test
  public void purchaseTickets_tooManyTicketsOneRequest_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 21);

    final var exception = assertInvalidPurchaseException(() -> {
      this.ticketService.purchaseTickets(accountId, request);
    });

    assertExceptionContains(exception, "Must purchase between 1 and 20 tickets.");
  }

  @Test
  public void purchaseTickets_tooManyTicketsMultipleRequests_throwsException() {
    final Long accountId = Long.valueOf(1);
    final int numRequests = 21;
    TicketTypeRequest[] requests = new TicketTypeRequest[numRequests];

    for (int i = 0; i < numRequests; i++) {
      requests[i] = new TicketTypeRequest(Type.ADULT, 1);
    }

    final var exception = assertInvalidPurchaseException(() -> {
      this.ticketService.purchaseTickets(accountId, requests);
    });

    assertExceptionContains(exception, "Must purchase between 1 and 20 tickets");
  }

  @Test
  public void purchaseTickets_accountIdLessThan1_throwsException() {
    Long accountId = Long.valueOf(0);
    TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);

    final var exception = assertInvalidPurchaseException(() -> {
      ticketService.purchaseTickets(accountId, request);
    });

    assertExceptionContains(exception, "accountId must be greater than 0.");

  }

  @Test
  public void purchaseTickets_oneAdult_chargesCorrectly() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 1);

    this.ticketService.purchaseTickets(accountId, request);

    verify(mockPaymentService).makePayment(accountId, Type.ADULT.getPrice());
  }

  @Test
  public void purchaseTickets_oneAdult_booksSeat() {
    final int numSeats = 1;
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, numSeats);

    this.ticketService.purchaseTickets(accountId, request);

    verify(mockReservationService).reserveSeat(accountId, numSeats);
  }

  @Test
  public void purchaseTickets_oneAdultManyChildren_chargesCorrectly() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests =
        {new TicketTypeRequest(Type.ADULT, 1), new TicketTypeRequest(Type.CHILD, 5)};

    this.ticketService.purchaseTickets(accountId, requests);

    final int expectedCost = Type.ADULT.getPrice() + Type.CHILD.getPrice() * 5;
    verify(mockPaymentService).makePayment(accountId, expectedCost);
  }

  @Test
  public void purchaseTickets_childNoAdult_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest request = new TicketTypeRequest(Type.CHILD, 1);

    final var exception = assertInvalidPurchaseException(() -> {
      this.ticketService.purchaseTickets(accountId, request);
    });

    assertExceptionContains(exception, "At least 1 adult ticket must be purchased.");
  }

  @Test
  public void purchaseTickets_lessInfantsThanAdults_chargesCorrectly() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests =
        {new TicketTypeRequest(Type.ADULT, 2), new TicketTypeRequest(Type.INFANT, 1)};

    this.ticketService.purchaseTickets(accountId, requests);

    final int expectedCost = 2 * Type.ADULT.getPrice() + Type.INFANT.getPrice();
    verify(this.mockPaymentService).makePayment(accountId, expectedCost);
  }

  @Test
  public void purchaseTickets_lessOrEqualInfantsToAdults_booksSeats() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests =
        {new TicketTypeRequest(Type.ADULT, 2), new TicketTypeRequest(Type.INFANT, 2)};

    this.ticketService.purchaseTickets(accountId, requests);

    verify(this.mockReservationService).reserveSeat(accountId, 2);
  }

  @Test
  public void purchaseTickets_moreInfantsThanAdults_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests =
        {new TicketTypeRequest(Type.ADULT, 1), new TicketTypeRequest(Type.INFANT, 2)};

    final var exception = assertInvalidPurchaseException(() -> {
      this.ticketService.purchaseTickets(accountId, requests);
    });

    assertExceptionContains(exception, "Must have at least 1 adult per infant.");
  }

  private InvalidPurchaseException assertInvalidPurchaseException(Runnable fn) {
    try {
      fn.run();
      fail("Expected to throw InvalidPurchaseException");
    } catch (InvalidPurchaseException exception) {
      return exception;
    }

    /*
     * Should never reach here: either fn throws and we return the exception, or it doesn't and fail
     * throws.
     */
    return null;
  }

  private void assertExceptionContains(Exception exception, String pattern) {
    final String message = exception.getMessage();
    assertTrue(String.format("Expected '%s' to contain '%s'.", message, pattern),
        message.contains(pattern));
  }
}
