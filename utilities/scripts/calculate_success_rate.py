import http.client
import json
import ssl
from dataclasses import dataclass


KNOWN_SERVER_URLS = {
    "https://matterhorn.teamcity.com",
    "https://buildserver.labs.intellij.net"
}

TOKEN = '%TeamCityToken%'

HEADERS = {
    'Accept': 'application/json',
    'Authorization': f'Bearer {TOKEN}'
}


@dataclass
class Statistics:
    successes: int
    failures: int

    @property
    def total(self) -> int:
        return self.successes + self.failures

    @property
    def success_rate(self) -> float:
        return 0 if self.total == 0 else self.successes / self.total



@dataclass
class Build:
    id: int
    buildTypeId: str
    status: str

    @property
    def is_successful(self) -> bool:
        return self.status == "SUCCESS"


def get_connection():
    server_url = "%teamcity.serverUrl%"
    if server_url not in KNOWN_SERVER_URLS:
        raise ValueError(f"Unknown server url: {server_url}")
    host = server_url[8:]
    context = ssl._create_unverified_context()
    connection = http.client.HTTPSConnection(host, context=context)
    return connection


def get_response(connection, url, headers):
    connection.request("GET", url, headers=headers)
    response = connection.getresponse()
    data = response.read().decode()
    return data


def get_dependent_builds(connection, headers) -> list[Build]:
    build_id = "%teamcity.build.id%"
    url = f"/app/rest/builds?locator=snapshotDependency:(to:(id:{build_id}),recursive:false),defaultFilter:false,count:10000"
    response = get_response(connection, url, headers)
    content = json.loads(response)
    builds = content["build"]
    return [
        Build(
            id=build["id"],
            buildTypeId=build["buildTypeId"],
            status=build["status"]
        )
        for build in builds
    ]


def get_statistics(connection, headers) -> Statistics:
    dependent_builds = get_dependent_builds(connection, headers)
    successes = sum(build.is_successful for build in dependent_builds)
    failures = len(dependent_builds) - successes
    return Statistics(successes=successes, failures=failures)


def print_statistics(stats: Statistics) -> None:
    print(f"Total tasks: {stats.total}")
    print(f"Total successful tasks: {stats.successes}")
    print(f"##teamcity[buildStatus text='Success rate: {stats.success_rate:.2f} ({stats.successes} of {stats.total})']")


def main() -> None:
    connection = get_connection()
    statistics = get_statistics(connection, HEADERS)
    connection.close()

    print("TEAMCITY RUN ID: %teamcity.run.id%")
    print_statistics(statistics)


if __name__ == "__main__":
    main()