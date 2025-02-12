# Introduction Periodic High Utility Itemsets (PHUIs) Web Application
## Feature
The local web app to finding Periodic High Utility Itemsets develop using Java Spring, SPMF API.

- Input: transaction database with SPMF format, details description on document page. User defined parameters.
- Output: All PHUIs satisfy the conditions.

## System Re-requisites 
- JDK 21 or newer.
- OS: Windows 11, Linux.
- IDE: Intellij, Eclipse

# Installation Guide
## Step 1: Clone project repo from GitHub
Download `zip` file from [GitHub repo](https://github.com/thnhan1/phm-demo) of using Git Command below.
```powershell
git clone https://github.com/thnhan1/phm-demo.git
```

## Step 2: Build spring boot application using Maven
Change directory to root folder of Spring Boot project (located of `pom.xml`). Using Maven command to build `.jar` file of application.
- Windows:
```bash
./mvnw.cmd clean package
```
- Linux:
```powershell
./mvnw clean package
```

Build result located in root folder: `target/phm-0.0.1-SNAPSHOT.jar`

## Step 3: Run Spring Boot Application
Using IntelliJ IDEA or using command.

```powershell
java -jar target/phm-0.0.1-SNAPSHOT.jar
```

## Step 4: Open Web browser
Access url [http://localhost:8080](http://localhost:8080) to using application.

# Website Structure
## Website map
- Home page (`/`: Input form of PHM algorithm.
- Algorithm theorem page (`/algorithm`): Summary PHM algorithm
- Datasets page (`/dataset`): list of open dataset resources available for run PHM algorithm.
- Result page (`/run`): result of PHM algorithm, you can download result to computer.
## Screenshot

<dl>
<dd>


- Home page

![./images/homepage_sc](./images/homepage_sc.png)
</dd>
<dd>

- Result page

![./images/results](./images/result.png)
</dd>
</dl>


# References:

- [An Open-Source Data Mining Library](https://www.philippe-fournier-viger.com/spmf/) version v2.62 released the 12th June 2024.

# Contributes
- Pull this repo
- Request feature using GitHub issues.
- Contact to me: @thanhan1

