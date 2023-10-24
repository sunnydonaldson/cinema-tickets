package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements {@see TicketService} with functionality for reserving and paying for seats.
 */
public class TicketServiceImpl implements TicketService {
    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    /**
     * Returns the default TicketServiceImpl
     * 
     * @return a TicketServiceImpl with default TicketPaymentService and SeatReservationService.
     */
    public TicketServiceImpl() {
        this(new TicketPaymentServiceImpl(), new SeatReservationServiceImpl());
    }

    /**
     * Returns a custom TicketServiceImpl with dependency injection.
     * 
     * @return a TicketServiceImpl with injected {@code paymentService} and
     *         {@code reservationService}.
     */
    TicketServiceImpl(TicketPaymentService paymentService,
            SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    /**
     * Charges payment and reserves seats for all {@param ticketTypeRequests}.
     * 
     * @param accountId for the user, must be greater than 0.
     * @param ticketTypeRequests a list of requests to fulfil containing at least 1 adult, with less
     *        or equal infants than adults, and less than 20 total total tickets.
     * @throws InvalidPurchaseException if the constraints on the params above are violated.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {
        try {
            checkPreconditions(accountId, ticketTypeRequests);
        } catch (RuntimeException e) {
            throw new InvalidPurchaseException(e);
        }

        this.reservationService.reserveSeat(accountId, numSeats(ticketTypeRequests));
        this.paymentService.makePayment(accountId, totalCost(ticketTypeRequests));
    }

    private static final void checkPreconditions(Long accountId,
            TicketTypeRequest... ticketTypeRequests) {
        checkNotNull(accountId, "accountId must not be null.");
        checkNotNull(ticketTypeRequests, "ticketTypeRequests must not be null.");

        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            int numTickets = request.getNoOfTickets();
            Type type = request.getTicketType();

            if (type.equals(Type.ADULT)) {
                adultCount += numTickets;
            } else if (type.equals(Type.CHILD)) {
                childCount += numTickets;
            } else {
                infantCount += numTickets;
            }
        }
        int totalCount = adultCount + infantCount + childCount;

        checkArgument(accountId > 0, "accountId must be greater than 0.");
        checkArgument(adultCount >= 1, "At least 1 adult ticket must be purchased.");
        checkArgument(totalCount <= 20, "Must purchase between 1 and 20 tickets.");
        checkArgument(infantCount <= adultCount, "Must have at least 1 adult per infant.");
    }

    private static final int numSeats(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests).filter(r -> !r.getTicketType().equals(Type.INFANT))
                .mapToInt(TicketTypeRequest::getNoOfTickets).sum();
    }

    private static final int totalCost(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getTotalCost).sum();
    }
}
