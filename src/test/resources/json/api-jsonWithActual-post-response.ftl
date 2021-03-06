{
  "active": true,
  "addresses": {
    "home": {
      "age": 100,
      "city": "${actual.addresses['home'].city}",
      "state": "Colorado",
      "zipcode": "80023"
    },
    "work": {
      "age": 100,
      "city": "Denver",
      "state": "Colorado",
      "zipcode": "80202"
    }
  },
  "age": ${
actual.age
},
  "bar": false,
  "favoriteMonth": 5,
  "favoriteYear": 1976,
  "ids": {
    "0": 1,
    "1": 2
  },
  "lifeStory": "${actual.lifeStory}",
  "locale": "en_US",
  "securityQuestions": [
    "one",
    "two",
    "three",
    "four"
  ],
  "siblings": [
    {
      "active": false,
      "addresses": {},
      "bar": false,
      "ids": {},
      "name": "Brett",
      "siblings": []
    },
    {
      "active": false,
      "addresses": {},
      "bar": false,
      "ids": {},
      "name": "Beth",
      "siblings": []
    }
  ],
  "type": "COOL"
}