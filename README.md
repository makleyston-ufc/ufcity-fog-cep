# UFCity CEP (Complex Events Processing)

UFCity CEP is a software component that integrates the UFCity solution for smart cities. This component belongs to Fog Computing and has the following characteristics:

* Scans event streams for predefined patterns.
* Updates your EPLs at runtime.
* Notifies Edge Computing devices for actions in the environment.

## How to use?
Create a configuration file `config.yaml`:
- ./
    - ufcity-cep.jar
    - config.yaml

Example of a `ufcity-cep.config` file:
```
fog-computing:
  - address: 10.0.0.112
  - port: 1883
cloud-computing:
  - address: 10.0.0.112
  - port: 1883
database:
  - address: mongo
  - port: 27017
  - username: root
  - password: example
```

Note: Into the Docker environment can use the hostname instead IP.

#### Download UFCity Semantic
Download: [ufcity-fog-cep-1.0-SNAPSHOT.jar](build%2Flibs%2Fufcity-fog-cep-1.0-SNAPSHOT.jar)

#### Running the UFCity Semantic
`java -jar ufcity-fog-cep-1.0-SNAPSHOT.jar`