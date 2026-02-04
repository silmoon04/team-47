### Meeting 1: First Consultant Meeting
Mon 2nd February, 14:30 - 15:10 | Fatimah, Silmoon, Alexandra, Chris, Shakeel | In-Person (Shakeel - Online)

- discussed interactions with other teams (databases, interfaces and methods to interact with them), potential regular meetings after week 5
- reviewed agile methods (user stories) vs use case diagrams
- note that we must share the same architecture as our partner teams, mainly for databases 
- note not to allow full access to our database by other teams - instead restrict (read-only access) to only the required tables
- first project diary submission on the 27th of February is a somewhat informal submission, it is not graded however advice can be offered

### Discussion in consultant meeting:
Replace with user stories
Apply ranking to user stories (light touch statements of what is required)
Rather than referring to use cases 
Replace use cases with user stories id with names and provide ranking which are more important to implement.
Statement how you see importance based on user stories. 
What is mor important using initial statement using customers 
Our views. 
No none functional requirements but put place holder. “This is left intentional blank not providing as we were told so”.
User stories are functional requirements. 
Componemt diagram with subsystem that are more indepth such as third party components.
Interfaces must be fixed, level of detail captures by parameters. 
CA 
PU tightly works with
How to access inventory 
Accepting payments expectation is to use their connection to payment facilities.
What are the methods that need to be defined. 
Architecture must be the same for all teams.
### Diaries what to include:
-	Account of what you have done 
-	Time spent
-	Looked at twice first deliverable and the second to judge individual contribution. 
Teams interact with our data base by having access to tables. Unconstrained access (not ideal) or access only to few tables that are related. 
Implement data storage will just be a matter of switching from our proxy to the database provided. We can implement our own proxy that can read and implement data.


### Meeting 2: First Client meeting 
Wed 4th February, 12:00-13:00 | Fatimah, Silmoon, Alexandra, Chris, Hamza | In-Person
- no age restrictions required for certain products
- each account should only have one role, but this doesnt exclude a user from having multiple accounts (hypothetically)
- some reports are more important than others - note this when doing priority table
- race conditions: person who pays first is prioritised in the case where only one item is left, regardless of if it's in-store or online
- public users should also be able to track orders
- order is only considered successful once the payment has gone through
- we rely on team A's catalog when ordering stock for our shop
- our system specifinally will have our own catalog, user database etc
- we may have items in our own catalog that haven't been bought from InfoPharma (team A), ie own brand items
- no specific password constraints required
- one parameter for every item should be "low stock level" - this is the value that determines whether an item appears in a low stock report or not
- if someone orders a quantity greater than what is available, they should be asked to retry and be told the maximum they can buy (ie the number left in stock)
- team C relies on our inventory, they need to access our database but they only decrease values, not increase
- admin SOLELY deals with user accounts
- manager can do everything a pharmacist can do, plus more (ability to restore defaulted accounts etc)
- pharmacist is essentially the "merchant" modelled in the scenario
