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

public class TicketServiceImpl implements TicketService {
    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;
    /**
     * Should only have private methods other than the one below.
     */
    public TicketServiceImpl() {
        this(new TicketPaymentServiceImpl(), new SeatReservationServiceImpl());
    }

    TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }
    
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        try {
            checkPreconditions(accountId, ticketTypeRequests);
        } catch (IllegalArgumentException e) {
            throw new InvalidPurchaseException(e);
        }

        this.reservationService.reserveSeat(accountId, numSeats(ticketTypeRequests));
        this.paymentService.makePayment(accountId, totalCost(ticketTypeRequests));
    }

    private static final void checkPreconditions(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        for (TicketTypeRequest request: ticketTypeRequests) {
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
        return Arrays.stream(ticketTypeRequests)
            .filter(r -> !r.getTicketType().equals(Type.INFANT))
            .mapToInt(TicketTypeRequest::getNoOfTickets)
            .sum();
    }

    private static final int totalCost(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
            .mapToInt(TicketTypeRequest::getTotalcost)
            .sum();
    }
}
