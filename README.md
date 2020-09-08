# vertigo-extensions
=================

[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.vertigo/vertigo.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.vertigo%22)

Vertigo extensions are mini-appplications that provide **high value services** on a given topic.
Use them to enhance your applications!

Each extension can be used :
 - as a dependency to your existing vertigo app
 - as a standalone app for microservices architectures


## vertigo-commons
### A set of common tools 

* __analytics__ : track your process calls, time & errors
* __cache__ : keep your objects in memory to improve performance
* __codec__ : transform an object into another. (built-in codecs : HTML, SHA1, Base64, Compress, Serialize) 
* __daemon__ : manage all your application daemons (registering, stats) 
* __eventbus__ : a simple event bus for handling events in your app  
* __node__ : a simple node management for cluster applications (topology, health, config)
* __config__ : read configs for your application (overridable, externalizable, aggregate multiple configs)
* __peg__ : a simple parser for your [DSL](http://en.wikipedia.org/wiki/Domain-specific_language)
* __script__ : transform a simple String in an executable script right from your code  (because sometimes you need to merge code and data)
* __transaction__ : simple transaction management 

## vertigo-database
### A simple data access to your databases

* __sql__ : with built-in handlers : Oracle, MSSql, Postgresql, H2

## vertigo-dynamo
### A simple way to define the model for your application and provide useful api over it like storage, search, etc...
  
* __collections__ : collections tools (built-in : fulltext indexation, facetting, filtering)   
* __criteria__ : a unique api to build filters (predicates, sql)
* __domain__ : top-2-bottom POJO to simplify communication between layers from Database to GUI/WS
* __environment__ : initialize your components from different sources (built-in : powerdesigner, DSL, Java annotations)
* __file__ : manage file creation
* __kvstore__ : key/value datastore
* __store__ : simple access to persistence layer  (built-in : route by object type, CRUD operations, NN operations)
* __search__ : simple search api
* __task__ : create and manage various tasks (such as direct queries to databases)


## vertigo-account
### A simple user management system, not only technical.

* __authentication__ : provide a set of connectors to easily manage your end users authentication in your app
* __authorization__ : userSession and security tools to check access to resources (by user roles and/or data properties)   
* __identity__ : a way to store and identify your user accounts


## vertigo-vega
### Share your apps to others.

* __rest__ : Add a REST access layer to your application. Mainly oriented for production-ready Single-Page-Applications. Provides a production-ready security level.


## vertigo-studio
### A set of tools to help you through developpement
* __mda__ : Model Driven Architecture with tools to generate source code (java, sql, js, ts), multilingual properties...
* __reporting__ : a set of tools to build indicators about your app
* __ui__ : more to come

## vertigo-social [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-yes-green.svg)]()
### Add collaboration features to help your users

* __comment__ :  a simple way to add comments and metadata to business objects
* __notification__ : a standalone way to push notification to users
* __mail__ : a simple api to send mails
* __handle__ : *WIP*

## vertigo-orchestra [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-yes-green.svg)]()
### Manage, execute and monitor background operations and communications.
For more details see [here](/vertigo-orchestra/)

## vertigo-quarto [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-yes-green.svg)]()
### Publishing managment.

* __converter__ : obviously, converts your documents from one format to another
* __publisher__ : lightweight publishing tool. Produces documents from user defined templates and application data. Templates are really easy to modify because they are just ODT or DOCX with tags.
* __export__ : exports collections and object to usefull files formats (built-in: CSV, PDF, RTF, XLS)

## vertigo-struts2 [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-yes-green.svg)]()
### Bridge to use vertigo for Struts2 applications.

## vertigo-ui [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-yes-green.svg)]()
### Bridge to use vertigo for SpringMVC/Thymeleaf applications, use [VueJS](https://vuejs.org) & [Quasar](https://quasar.dev) for UI.

## vertigo-stella : [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-no-red.svg)]()
* __node__ : node of worker for distributed operations
* __work__ : process, shedule or distribute your tasks

## vertigo-dashboard [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-yes-green.svg)]()
Create dashboard for analytics. Using Timeseries database.

## vertigo-ledger [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-yes-green.svg)]()
POC api for using blockchain data proof.

## vertigo-rules [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-no-red.svg)]()
* Simple rule engine. Can be used in a standalone use case or integrated with the workflow module.

## vertigo-workflow [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-no-red.svg)]()
* Simple workflow module to handle linear workflows. A rule system can be used to dynamically skip activities.

## vertigo-audit [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-no-red.svg)]()

## vertigo-adapters [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-no-red.svg)]()
Standalone adapters to easily use third party services (Twitter, IFTTT, etc...) with your vertigo app

## vertigo-geo [![Ready to use?](https://img.shields.io/badge/Ready%20to%20use%3F-no-red.svg)]()

-----

## Additional note for [Eclipse](https://www.eclipse.org/) users
A plugin named [KSP Plugin](https://github.com/sebez/vertigo-chroma-kspplugin) developped by [sebez](https://github.com/sebez/) enhances Developer Experience.

#License
                Copyright (C) 2017-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
                KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
                
                Licensed under the Apache License, Version 2.0 (the "License");
                you may not use this file except in compliance with the License.
                You may obtain a copy of the License at
                
                http://www.apache.org/licenses/LICENSE-2.0
                
                Unless required by applicable law or agreed to in writing, software
                distributed under the License is distributed on an "AS IS" BASIS,
                WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                See the License for the specific language governing permissions and
                limitations under the License.
