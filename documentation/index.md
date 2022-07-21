## Ecosystem and Subprojects
The Ecosystem directory contains each project I am working on. I might be incorporating different languages into this Git for the explicit purpose of having a centralized point where I can copy *all* of the related source code, and build dynamic configurations as requested. I also want to avoid having the projects spread apart, as I am likely to work across multiple projects in one sitting.

### Sapphire
Sapphire constitutes a fully built assistant, meant to be a drop-in replacement for something like Google Assistant or Alexa. 

#### Personality
This is an upgrade module for Sapphire. The intention is to query state, status, and memory to generate an 'organic' emotional response that gets factored in to Sapphires responses. It should be tied to real-life analogs for the phone, such as batter, memory, etc

### Spellbook
Spellbook constitutes a space for creating 'smart' text entries, such as project notes, journal entries, wikis, as well as a place for reading the documentation related to these various projects (which ship as text files w/ each project). It also offers a UI for running background programs made available by the Sapphire Framework

### Sapphire Framework
The Sapphire Framework is a support library that is used across operating systems for quickly integrating systems into your ecosystem, or creating custom personal assistants. Most projects rely on some portion of the Sapphire Framework to work properly.