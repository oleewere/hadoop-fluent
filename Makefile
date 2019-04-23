# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

package: build
	./gradlew distTar

build: clean
	./gradlew jar copyDeps

clean:
	./gradlew clean

rpm: package
	./gradlew rpm

deb: package
	./gradlew deb

docker-build: package
	docker build -t oleewere/hadoop-fluent:latest .

docker-sample:
	cd sample && ./up.sh

docker-sample-with-build: docker-build docker-sample

all: deb