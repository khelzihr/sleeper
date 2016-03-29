# Sleeper
Sleeper is a project for Stockholm University course IB906C, with the end goal to create an application 
that uses different network protocols to communicate over the Internet.

## Overview
Sleeper is a Java application that listens for a keyphrase and executes a command when the keyphrase is found.

To listen for a keyphrase, Sleeper uses a Provider that performs the connection to an external source to fetch data.

The Provider may use a Parser to parse the incoming data to find the keyphrase.

## Providers

The following providers are included:

* GUMProvider - Generates a GuerrillaMail address daily and polls it for incoming mails. Returns true if a Parser finds the keyphrase in a mail.
* HTTPProvider - Polls a web page and sends the data to a Parser. Supports HTTP/HTTPS but will only accept HTTP 200 OK status

## Parsers

* PlainTextParser - Treats incoming data as a String and compares it to the keyphrase String. If the key phrase is found in the data, the action will be triggered.

