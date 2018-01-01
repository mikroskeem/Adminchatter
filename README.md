# Adminchatter

An awesome, highly configurable adminchat plugin for BungeeCord

## Requirements
- Java 8

## Commands
- **/adminchatter**
    * Description:
        - Reloads plugin
    * Permissions:
        - **adminchatter.reload**

- **/adminchat**
    * Aliases:
        - _/ac_
    * Description:
        - Sends message to admin chat
    * Permissions:
        - **adminchatter.chat** (needed for receiving players as well)

- **/adminchattoggle**
    * Aliases:
        - _/actoggle_
        - _/act_
    * Description
        - Toggles feature of sending your main chat to admin chat.
    * Permissions:
        - **adminchatter.chat**

## Building
On Linux, clone this repository and do `./gradlew build` in project's directory using terminal.  
On Windows, clone this repository, open cmd and `cd` to project directory and do `gradlew.bat build` 
        