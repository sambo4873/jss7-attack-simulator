# SS7 Attack Simulator based on RestComm's jss7.

## Introduction

Open Source Java SS7 attack simulator that makes it possible to simulate some publicly disclosed attacks on the SS7 network.

This project is part of an ongoing Master Thesis at NTNU Gjøvik, Norway.

## License

SS7 Attack Simulator is licensed under the Free Open Source [GNU Affero GPL v3.0](http://www.gnu.org/licenses/agpl-3.0.html).

## Downloads

Current nightly builds can be downloaded from the project's [Jenkins CI Server](https://jensen.ninja/jenkins/).

## Instructions

* How to run the simulator:
  * Download latest build artifact from the [Jenkins CI
  Server](https://jensen.ninja/jenkins/job/jss7-attack-simulator/).
  * Unzip the file.
  * The simulator is launched with the script:
      RELEASE_FOLDER/ss7/restcomm-ss7-simulator/bin/run.sh
  * Run
``` 
run.sh help 
```
    or
```
run.sh attack_simulation help 
```
    for help on how to run the simulator.

* The currently supported simple attacks are:
  * location:ati
  * location:psi
  * intercept:sms

## Wiki

Read the [wiki](https://github.com/polarking/jss7-attack-simulator/wiki) for more information.

## Build From Source

Instruction will be added to the wiki shortly.

