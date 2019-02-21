test-coverage:
	sbt coverage test coverageReport ; xdg-open target/scala-2.12/scoverage-report/index.html
