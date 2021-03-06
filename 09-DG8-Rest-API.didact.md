REST API
The Data Grid REST API lets you monitor, maintain, and manage Data Grid deployments and provides access to your data. Although the REST API has a vast amount of features that one can use, in our lab we will demonstrate some of them. For more details on the documentation on the Red Hat Data Grid REST API. Please visit the documentation page. Infinispan servers provide RESTful HTTP access to data through a REST endpoint built on Netty.

Supported Formats
You can write and read data in different formats and Data Grid can convert between those formats when required.

The following "standard" formats are interchangeable: - application/x-java-object - application/octet-stream - application/x-www-form-urlencoded - text/plain

We are using this example from the very good lists of demo collection on github: https://github.com/infinispan-demos/links We will do the following

Create a Cache in infinispan with Lucene support
We will define our protostream
We will load bulk data into the cache
we will use search queries directly into the cache.
Lets get started.

The Pokeman Example
First; what is a Pokemon, incase some of us didn’t know. Reminiscences from the GameBoy the creatures that inhabit the world of Pokémon are also called Pokémon. Many species of Pokémon are capable of evolving into a larger and more powerful creature. The change is accompanied by stat changes, generally a modest increase, and access to a wider variety of attacks. There are multiple ways to trigger an evolution including reaching a particular level, using a special stone, or learning a specific attack. For example, at level 16 Bulbasaur is capable of evolving into Ivysaur. Most notably, the Normal-type Eevee is capable of evolving into eight different Pokémon: Jolteon (Electric), Flareon (Fire), Vaporeon (Water), Umbreon (Dark), Espeon (Psychic), Leafeon (Grass), Glaceon (Ice), and Sylveon (Fairy). In Generation VI, a new mechanic called Mega Evolution—as well as a subset of Mega Evolution called Primal Reversion—was introduced into the game. Unlike normal evolution, Mega Evolution and Primal Reversion last only for the duration of a battle, with the Pokémon reverting to its normal form at the end. Forty-eight Pokémon are capable of undergoing Mega Evolution or Primal Reversion as of the release of Sun and Moon. In contrast, some species such as Castform, Rotom, Unown, and Lycanroc undergo form changes that may provide stat buffs or changes and type alterations but are not considered new species. Some Pokémon have differences in appearance due to gender. Pokémon can be male or female, male-only, female-only, or genderless

Setup
All the project sources are in the project dg8-restapi. We will be using Curl and our browser.

Lets open up a new terminal in the CodeReady workspace. Make sure you are logged into Openshift.

Run the following command on the terminal which will get the password for user developer and store it in and environment variable.

    export PASSWORD=$(oc get secret datagrid-service-generated-secret -o jsonpath="{.data.identities\.yaml}" | base64 --decode | awk 'NR==3' | awk '{print $2}')
So we are using the same service we created from the operator previously.

We will also need our loadbalancer address to reach the datagrid server, you can do that by running the following command in the terminal. which will set the LoadBalancer address to $LB in the terminal

    export LB="http://$(oc get services | grep datagrid-service-external | awk '{ print $4 }'):11222"
At anytime if you close the terminal, you will need to run these two commands again. To get the password and the loadbalancer

First lets register our protobuf
By now you already know what protobuf is from previous labs as well as you might know what Pokemon’s are. Perfect time to introduce our datastructure.

Lets see whats our datastructure is like.

/**
  * @Indexed
  */
message Pokemon  {
  repeated string abilities = 1;
  optional float against_bug = 2;
  optional float against_dark = 3;
  optional float against_dragon = 4;
  optional float against_electric = 5;
  optional float against_fairy = 6;
  optional float against_fight = 7;
  optional float against_fire = 8;
  optional float against_flying = 9;
  optional float against_ghost = 10;
  optional float against_grass = 11;
  optional float against_ground = 12;
  optional float against_ice = 13;
  optional float against_normal = 14;
  optional float against_poison = 15;
  optional float against_psychic = 16;
  optional float against_rock = 17;
  optional float against_steel = 18;
  optional float against_water = 19;
  optional int32 attack = 20;
  optional int32 base_egg_steps = 21;
  optional int32 base_happiness = 22;
  optional int32 base_total = 23;
  optional string capture_rate = 24;

  /* @Field(index=Index.YES, analyze = Analyze.YES, store = Store.NO) */
  optional string classfication = 25;
  optional int32 defense = 26;
  optional int32 experience_growth = 27;
  optional float height_m = 28;
  optional int32 hp = 29;

  /* @Field(index=Index.YES, analyze = Analyze.YES, store = Store.NO) */
  optional string japanese_name = 30;

  /* @Field(index=Index.YES, analyze = Analyze.YES, store = Store.NO) */
  optional string name = 31;
  optional float percentage_male = 32;
  optional int32 pokedex_number = 33;
  optional int32 sp_attack = 34;
  optional int32 sp_defense = 35;
  optional int32 speed = 36;

  /* @Field(index=Index.YES, analyze = Analyze.YES, store = Store.NO) */
  optional string type1 = 37;
  optional string type2 = 38;
  optional float weight_kg = 39;
  optional int32 generation = 40;
  optional int32 is_legendary = 41;
}
In our project dg8-restapi open the file pokemon.proto and paste the above datastructure. The structure defines the capabilites of a Pokemon

When caches are indexed, or specifically configured to store application/x-protostream, you can send and receive JSON documents that are automatically converted to and from Protostream.

You must register a protobuf schema for the conversion to work.

To register protobuf schemas via REST, invoke a POST or PUT in the ___protobuf_metadata cache as in the following command

  curl -u developer:$PASSWORD -X POST --data-binary @./pokemon.proto $LB/rest/v2/caches/___protobuf_metadata/pokemon.proto
Create a Cache
Now lets create an indexed cache since we want to retrieve data at speed from our Cache and Lucene store.

curl -u developer:$PASSWORD -H "Content-Type: application/json" -d '{"distributed-cache":{"mode":"SYNC","indexing":{"auto-config":true,"index":"ALL"}}}' $LB/rest/v2/caches/pokemon
Bulk loading the REST endpoint
Now we will load all the pokemon data we have in a json format. You can view the json files in dg8-restapi/data

for Example Abra’s capabilities listed as follows

{
    "_type": "Pokemon",
    "abilities": "['Synchronize', 'Inner Focus', 'Magic Guard']",
    "against_bug": 2.0,
    "against_dark": 2.0,
    "against_dragon": 1.0,
    "against_electric": 1.0,
    "against_fairy": 1.0,
    "against_fight": 0.5,
    "against_fire": 1.0,
    "against_flying": 1.0,
    "against_ghost": 2.0,
    "against_grass": 1.0,
    "against_ground": 1.0,
    "against_ice": 1.0,
    "against_normal": 1.0,
    "against_poison": 1.0,
    "against_psychic": 0.5,
    "against_rock": 1.0,
    "against_steel": 1.0,
    "against_water": 1.0,
    "attack": 20.0,
    "base_egg_steps": 5120.0,
    "base_happiness": 70.0,
    "base_total": 310.0,
    "capture_rate": 200.0,
    "classfication": "Psi Pok\u00e9mon",
    "defense": 15.0,
    "experience_growth": 1059860.0,
    "height_m": 0.9,
    "hp": 25.0,
    "japanese_name": "Casey\u30b1\u30fc\u30b7\u30a3",
    "name": "Abra",
    "percentage_male": 75.4,
    "pokedex_number": 63.0,
    "sp_attack": 105.0,
    "sp_defense": 55.0,
    "speed": 90.0,
    "type1": "psychic",
    "type2": 0,
    "weight_kg": 19.5,
    "generation": 1.0,
    "is_legendary": 0.0
}
Lets run our loading script which is placed in our project dg8-restapi. Run the shell script in the terminal

./ingest-data.sh
The script run can take some time, wait for it to finish.

So by now we should have loaded about 801 Pokemon’s from the Pokemon universe.

So what does the script look like? Below you can se that we are loading each of the json files one by one to the cache rest end point

status=0
for f in data/*.json
do
  curl -u developer:$PASSWORD -XPOST --data-binary @${f}  -H "Content-Type: application/json; charset=UTF-8"  $LB/rest/v2/caches/pokemon/$(basename $f .json)
  let status=status+1
  echo  "Imported $f (total $status pokemons)"
done
Query the data
Get all Pokemons:

   $LB/rest/v2/caches/pokemon?action=search&query=from%20Pokemon
Count Pokemons by generation:

   select count(p.name) from Pokemon group by generation
   $LB/rest/v2/caches/pokemon?action=search&query=select%20count(p.name)%20from%20Pokemon%20p%20group%20by%20generation
Do a full text search on the name

  $LB/rest/v2/caches/pokemon?action=search&query=from%20Pokemon%20where%20name:%27pikachu%27
Select top 5 Pokemons that can better withstand fire:

  $LB/rest/v2/caches/pokemon?action=search&query=from%20Pokemon%20order%20by%20against_fire%20asc&max_results=5
Get Pokemon by key (name)

    $LB/rest/v2/caches/pokemon/Whismur
Recap
You how the REST API works
You created a cache and protobuf via REST API
You loaded bulk data into the cache
And finally you queried through that data.
*Congratulations!! you have completed the this lab on REST API!!