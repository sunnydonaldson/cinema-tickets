package uk.gov.dwp.uc.pairtest.domain;

/**
 * Immutable Object
 */
public class TicketTypeRequest {

    private int noOfTickets;
    private Type type;

    public TicketTypeRequest(Type type, int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public int getTotalCost() {
        return this.type.getPrice() * this.noOfTickets;
    }

    public enum Type {
        ADULT(20), CHILD(10), INFANT(0);

        private final int price;

        Type(int price) {
            this.price = price;
        }

        public int getPrice() {
            return price;
        }
    }
}
