# UFCity CEP (Complex Events Processing)

UFCity CEP is a software component that integrates the UFCity solution for smart cities. This component belongs to Fog Computing and has the following characteristics:

* Scans event streams for predefined patterns.
* Updates your EPLs at runtime.
* Notifies Edge Computing devices for actions in the environment.

## How to use?
Create a configuration file `ufcity-cep.config`:
- ./
    - ufcity-cep.jar
    - ufcity-cep.config

Example of a `ufcity-cep.config` file:
```
--fog-address: 172.23.0.4
--cloud-address: 172.23.0.5
--fog-port: 1883
--cloud-port: 1883
--database-address: mongo
--database-port: 27017
```

Note: Into the Docker environment can use the hostname instead IP.

#### Download UFCity Semantic
Download: [ufcity-fog-cep-1.0-SNAPSHOT.jar](build%2Flibs%2Fufcity-fog-cep-1.0-SNAPSHOT.jar)

#### Running the UFCity Semantic
`java -jar ufcity-fog-cep-1.0-SNAPSHOT.jar`