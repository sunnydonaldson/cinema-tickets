# Sunny Donaldson Cinema Tickets Java

## My Assumptions
I made some additional assumptions on top of yours:
* You said infants don't require seats because they'll sit in adults' laps,
  which I took to mean one infant per adult, so there must be more or equal adult tickets than infant tickets.
* Because you explicitly said no more than 20 tickets are allowed per booking- not 20 seats-
  I assumed the infants count towards the limit, as they do still require tickets despite not needing seats.

## Things to Note
* I would rather have used JUnit5 because it has a much nicer API for testing exceptions and parameterised testing,
  but I assumed you wanted me to use the specific version in the Maven dependencies.
* I assumed you'd be okay with me importing Guava for its Precondition class,
  as the methods I'm using from it are pretty trivial, but I could have implemented them myself if necessary.
* I assumed you didn't want me to touch the interfaces you provided, otherwise I would have added Javadoc to them.
* If I could have altered the interface (even though this wouldn't actually change its contract), I also would have removed the `throws InvalidPurchaseException` from the `TicketService#purchaseTickets` definition
  as `InvalidPurchaseException` inherits from `RuntimeException`. `RuntimeException` is an unchecked exception,
  meaning the compiler doesn't check for it at compile time. It's considered better practice to add an `@throws`
  tag to the Javadoc for unchecked exceptions instead.
* I assumed you'd be okay with me altering `TicketTypeRequest` because it's still immutable.
* I assumed that adding a public constructor to `TicketService` to allow dependency injection was okay.

## Things I'd Do With More Time
* Add better documentation, with usage examples.
* Use parameterised testing, but as I mentioned, the JUnit4 API for parameterised testing is really clunky,
  and I felt like it would harm readability. The JUnit5 parameterised testing API is nice,
  and I feel it'd actually improve readability.

## Things I'd Have Done Differently
It would have been nice to verify the extra assumptions I made regarding the inputs and which parts of the code I was allowed to alter,
but I didn't know who to contact.

## Running the tests
* make sure you have Maven installed
* navigate to the java root directory `cd cinema-tickets-java`
* run tests with Maven `mvn test`