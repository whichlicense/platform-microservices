# platform-microservices

## Installation Instructions

1. Set the `CR_PAT` environment variable to your GitHub token:

   ```bash
   export CR_PAT=<github-token>
   ```

2. Login to the GitHub Container Registry using your GitHub username and the `CR_PAT` token:

   ```bash
   echo $CR_PAT | docker login ghcr.io -u <github-username> --password-stdin
   ```

3. Create a Docker network named `galaxy`:

   ```bash
   docker network create galaxy
   docker network inspect galaxy
   ```

4. Start the `spectra` container, connecting it to the `galaxy` network and mapping port `8081`:

   ```bash
   docker run --name spectra --network galaxy -p 8081:8080 ghcr.io/whichlicense/platform-microservices/spectra:0.9.3
   ```

5. Start the `galileo` container, connecting it to the `galaxy` network and mapping port `8082`:

   ```bash
   docker run --name galileo --network galaxy -p 8082:8080 ghcr.io/whichlicense/platform-microservices/galileo:0.9.3
   ```

6. Start the `stellar` container, connecting it to the `galaxy` network and mapping port `8083`:

   ```bash
   docker run --name stellar --network galaxy -p 8083:8080 ghcr.io/whichlicense/platform-microservices/stellar:0.9.3
   ```

7. Start the `meteor` container, connecting it to the `galaxy` network and mapping port `8084`:

   ```bash
   docker run --name meteor --network galaxy -p 8084:8080 ghcr.io/whichlicense/platform-microservices/meteor:0.9.3
   ```

8. Start the `nebula` container, connecting it to the `galaxy` network and mapping port `8085`:

   ```bash
   docker run --name nebula --network galaxy -p 8085:8080 ghcr.io/whichlicense/platform-microservices/nebula:0.9.3
   ```

9. Finally, inspect the `galaxy` network to ensure successful setup:

   ```bash
   docker network inspect galaxy
   ```
