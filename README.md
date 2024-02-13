# 数据库系统实验——图书管理系统

## 关于本仓库

本仓库是 2023 春夏数据库系统春季综合实验的代码仓库

## 框架使用指南——Java

### 环境要求

- JDK 1.8.0 及以上，可通过 `java -version` 命令查看
- Apache Maven 3.6.3 及以上，可通过 `mvn -v` 命令查看

`resources` 目录下存放了数据库连接的相关配置以及Sql脚本

清理输出目录并编译项目主代码
``` bash
mvn clean compile
```

运行主代码
``` bash
mvn exec:java -Dexec.mainClass="Main" -Dexec.cleanupDaemonThreads=false
```

运行所有的测试
``` bash
mvn -Dtest=LibraryTest clean test
```

运行某个特定的测试
``` bash
mvn -Dtest=LibraryTest#parallelBorrowBookTest clean test
```

## License

在 MIT 许可下分发。有关更多信息，请参见 LICENSE。
