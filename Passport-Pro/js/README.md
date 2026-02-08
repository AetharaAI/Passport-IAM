# Passport JavaScript

This directory contains the UIs and related libraries of the Passport project written in JavaScript (and TypeScript).

## Directory structure

    ├── apps
    │   ├── account-ui                 # Account UI for account management i.e controlling password and account access, tracking and managing permissions
    │   ├── admin-ui                   # Admin UI for handling login, registration, administration, and account management
    │   └── passport-server            # Passport server for local development of UIs
    ├── libs
    │   ├── ui-shared                  # Shared component library between admin and account
    │   └── passport-admin-client      # Passport Admin Client library for Passport REST API
    ├── ...

## Data processing

Red Hat may process information including business contact information and code contributions as part of its participation in the project, data is processed in accordance with [Red Hat Privacy Statement](https://www.redhat.com/en/about/privacy-policy).

To speed up the build process, the following build flag can be used to disable the processing of these modules:

    -Dskip.npm

## Contributing

If you want to contribute please look at the [coding guidelines](CODING_GUIDELINES.md)