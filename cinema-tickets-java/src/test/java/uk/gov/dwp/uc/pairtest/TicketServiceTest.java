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
    this.ticketService = new TicketServiceImpl(
        this.mockPaymentService, this.mockReservationService);
  }

  @Test
  public void purchaseTickets_tooFewTickets_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {};

    purchaseTicketsThrowsInvalidPurchaseException(accountId,
        requests, "At least 1 adult ticket must be purchased.");
  }  

  @Test
  public void purchaseTickets_tooManyTicketsOneRequest_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, 21)
    };

    purchaseTicketsThrowsInvalidPurchaseException(accountId,
        requests, "Must purchase between 1 and 20 tickets.");
  }

  @Test
  public void purchaseTickets_tooManyTicketsMultipleRequests_throwsException() {
    final Long accountId = Long.valueOf(1);
    final int numRequests = 21;
    TicketTypeRequest[] requests = new TicketTypeRequest[numRequests];

    for (int i = 0; i < numRequests; i++) {
      requests[i] = new TicketTypeRequest(Type.ADULT, 1);
    }

    purchaseTicketsThrowsInvalidPurchaseException(accountId,
        requests, "Must purchase between 1 and 20 tickets.");
  }

  @Test
  public void purchaseTickets_accountIdLessThan1_throwsException() {
    Long accountId = Long.valueOf(0);
    TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, 1)
    };
    String expectedMessage = "accountId must be greater than 0.";

    purchaseTicketsThrowsInvalidPurchaseException(accountId, requests, expectedMessage);
  }

  @Test
  public void purchaseTickets_oneAdult_chargesCorrectly() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, 1)
    };

    this.ticketService.purchaseTickets(accountId, requests);

    verify(mockPaymentService).makePayment(accountId, Type.ADULT.getPrice());
  }

  @Test
  public void purchaseTickets_oneAdult_booksSeat() {
    final int numSeats = 1;
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, numSeats)
    };

    this.ticketService.purchaseTickets(accountId, requests);

    verify(mockReservationService).reserveSeat(accountId, numSeats);
  }

  @Test
  public void purchaseTickets_oneAdultManyChildren_chargesCorrectly() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, 1),
      new TicketTypeRequest(Type.CHILD, 5)
    };
    final int expectedCost = Type.ADULT.getPrice() + Type.CHILD.getPrice() * 5;

    this.ticketService.purchaseTickets(accountId, requests);

    verify(mockPaymentService).makePayment(accountId, expectedCost);
  }

  @Test
  public void purchaseTickets_childNoAdult_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.CHILD, 1)
    };
    final String expectedMessage = "At least 1 adult ticket must be purchased.";

    purchaseTicketsThrowsInvalidPurchaseException(accountId, requests, expectedMessage);
  }

  @Test
  public void purchaseTickets_lessInfantsThanAdults_chargesCorrectly() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, 2),
      new TicketTypeRequest(Type.INFANT, 1)
    };
    final int expectedCost = 2 * Type.ADULT.getPrice() + Type.INFANT.getPrice();

    this.ticketService.purchaseTickets(accountId, requests);

    verify(this.mockPaymentService).makePayment(accountId, expectedCost);
  }

  @Test
  public void purchaseTickets_lessOrEqualInfantsToAdults_booksSeats() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, 2),
      new TicketTypeRequest(Type.INFANT, 2)
    };

    this.ticketService.purchaseTickets(accountId, requests);

    verify(this.mockReservationService).reserveSeat(accountId, 2);
  }

  @Test
  public void purchaseTickets_moreInfantsThanAdults_throwsException() {
    final Long accountId = Long.valueOf(1);
    final TicketTypeRequest[] requests = {
      new TicketTypeRequest(Type.ADULT, 1),
      new TicketTypeRequest(Type.INFANT, 2)
    };
    final String expectedMessage = "Must have at least 1 adult per infant.";

    purchaseTicketsThrowsInvalidPurchaseException(accountId, requests, expectedMessage);
  }

  private void purchaseTicketsThrowsInvalidPurchaseException(Long accountId,
      TicketTypeRequest[] requests, String expectedMessage) {
    try {
      this.ticketService.purchaseTickets(accountId, requests);
      fail("Expected to throw InvalidPurchaseException");
    } catch (InvalidPurchaseException exception) {
      final String failureMessage = String.format(
          "Expected '%s' to contain '%s'",
          exception.getMessage(), expectedMessage
      );

      assertTrue(failureMessage,
          exception.getMessage().contains(expectedMessage));
    }
  }
}
