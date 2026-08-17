# Read Me First
The following was discovered as part of building this project:

* The JVM level was changed from '21' to '17', review the [JDK Version Range](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Versions#jdk-version-range) on the wiki for more details.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.0-SNAPSHOT/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.2.0-SNAPSHOT/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.0-SNAPSHOT/reference/htmlsingle/index.html#web)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.2.0-SNAPSHOT/reference/htmlsingle/index.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Command to generate UUID on database

Extension to support UUID in database
```postgresql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

```sql
uuid_generate_v4()
```

Backup database
```cmd
docker exec -t water-db pg_dump -U postgres dreamsbo > backup20260114.sql
```

Restore database
```cmd
cat PROD-2024_01_07_20_45_10.sql | docker exec -i --user postgres water-db psql -U postgres -d dreamsbo
```

Copy folder
```
docker cp <src-path> <container>:<dest-path> 
```

Copy from Contabo
```cmd
scp root@157.173.116.93:/root/app/myfile.sql ~/Downloads
```

Restaure your backup from local to contabo
```cmd
cat backup20260411-march2026-aligned.sql | ssh root@157.173.116.93 "docker exec -i --user postgres water-db psql -U postgres -d dreamsbo"
```

Copy from local to contabo
```cmd
scp backup20260530ForPROD.sql root@157.173.116.93:/app
```

