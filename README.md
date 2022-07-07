Introduction
========
This repository forked from https://github.com/OHDSI/WhiteRabbit. 

**WhiteRabbit** is a small application that can be used to analyse the structure and contents of a database as preparation for designing an ETL. 

This service wraps WhiteRabbit functional in Web-service, that used by **Perseus** https://github.com/SoftwareCountry/Perseus. 

Features
========
- Can scan databases in SQL Server, Oracle, PostgreSQL, MySQL, MS Access, Amazon RedShift, Google BigQuery, SAS files and CSV files
- The scan report contains information on tables, fields, and frequency distributions of values 
- Cutoff on the minimum frequency of values to protect patient privacy

Technology
============

- Java 17

Getting Started
===============

    docker build -t white-rabbit .
    docker run --name white-rabbit -d -p 8000:8000 -e SPRING_PROFILES_ACTIVE='docker' --network=perseus-net white-rabbit

License
=======
WhiteRabbit is licensed under Apache License 2.0
