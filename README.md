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
        - **adminchatter.chat.admin** (needed for receiving players as well)

- **/adminchattoggle**
    * Aliases:
        - _/actoggle_
        - _/act_
    * Description
        - Toggles feature of sending your main chat to admin chat.
    * Permissions:
        - **adminchatter.chat.admin**

See configuration file for more channels, you can add them as much as you want.  
Permission node is always `adminchatter.chat.<channel name>`.

## Building
On Linux, clone this repository and do `./gradlew build` in project's directory using terminal.  
On Windows, clone this repository, open cmd and `cd` to project directory and do `gradlew.bat build` 
        
