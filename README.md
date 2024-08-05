# onco-analytics-monitor

Diese Anwendung überwacht die konfigurierten Topics, ermittelt die Anzahl der Conditions nach ICD10-Gruppe sortiert
und zeigt diese in Echtzeit an.

Bei Änderungen wird die Tabelle des entsprechenden Topics kurz hervorgehoben.

![Screenshot](docs/screenshot.jpeg)

## Datenquellen und Datenhaltung

Es wird keine Datenbank benötigt. Alle Informationen werden aus den Kafka Topics bezogen. Dabei werden beim Neustart der
Anwendung alle verfügbaren Records aus den Topics neu eingelesen, initiale Statistiken erstellt, welche bei neu
eingehenden Records aktualisiert werden.