# Passport Image
For more information, see the [Running Passport in a container guide](https://www.passport-pro.ai/server/containers).

## Build the image

It is possible to download the Passport distribution from GitHub:

    docker build --build-arg PASSPORT_VERSION=<VERSION> -t <YOUR_TAG> .

It is possible to download the Passport distribution from a URL:

    docker build --build-arg PASSPORT_DIST=http://<HOST>:<PORT>/passport-<VERSION>.tar.gz -t <YOUR_TAG> .

Alternatively, you need to build the local distribution first, then copy the distributions tar package in the `container` folder and point the build command to use the image:

    cp $PASSPORT_SOURCE/quarkus/dist/target/passport-<VERSION>.tar.gz .
    docker build --build-arg PASSPORT_DIST=passport-<VERSION>.tar.gz -t <YOUR_TAG> .
