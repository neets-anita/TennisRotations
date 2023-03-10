const RotationsApp = {
  props: {},
  data() {
    return {
      players: [],
      numCourts: 2,
      numRotations: 5,
      startTime: "14:10",
      rotationDuration: 20,
      rotations: []
    };
  },
  computed: {
    hasPlayers() {
      return this.players.length > 3
    },
    hasRotations() {
      return this.rotations.length > 0
    }
  },
  methods: {
    addPlayer(player) {
      if (this.players.indexOf(player) < 0 && player.trim().length > 0) {
        this.players.push(player)
      }
    },
    removePlayer(player) {
      this.players.splice(this.players.indexOf(player), 1)
    },
    generateRotations() {
      this.numCourts = adjustNumCourts(this.numCourts, this.players.length)
      requestRotations(this.players, this.numCourts, this.numRotations)
      .then(result => {
        this.rotations = result.rotations
      });
    },
    resetPlayers() {
      this.players = []
    }
  }
};

const app = Vue.createApp(RotationsApp, {numCourts: 2});

app.component('add-player', {
  template: '#v-add-player-template',
  emits: ['addPlayer'],
  data() {
    return {
      playerName: ''
    };
  },
  computed: {
    hasEnteredPlayerName() {
      return this.playerName !== '';
    }
  },
  methods: {
    addPlayer(event) {
      if ((event.type === "keypress" && event.key === 'Enter') || event.type === "click") {
        this.$emit('addPlayer', this.playerName);
        this.playerName = '';
      }
    }
  }
});

app.component('players', {
  template: '#v-players-template',
  emits: ['removePlayer'],
  props: {
    players: Array
  },
  methods: {
    removePlayer(player) {
      this.$emit('removePlayer', player)
    }
  }
});

app.component('player', {
  template: '#v-player-template',
  emits: ['removePlayer'],
  props: {
    player: String
  }
});


app.component('rotations', {
  template: '#v-rotations-template',
  props: {
    rotations: Array,
    rotationDuration: Number,
    startTime: String
  },
  beforeUpdate() {
    generateRotationTimes(this.rotations, this.startTime, this.rotationDuration);
  },
  created() {
    generateRotationTimes(this.rotations, this.startTime, this.rotationDuration);
  }
});

app.mount('#v-rotations-app');

function requestRotations(players, numCourts, numRotations) {
  return fetch("/rotations",
      {
        headers: { 'Content-Type': 'application/json' },
        method: 'POST',
        body: JSON.stringify(
            {
              players: players,
              config: {
                numCourts: numCourts,
                numRotations: numRotations
              }
            }
        )
      })
  .then(response => {
    if (!response.ok) {
      throw new Error('API Error: '+ response.toString());
    }
    return response.json();
  });
}

function generateRotationTimes(rotations, startTime, rotationDuration) {
  const [hour, minutes] = startTime.split(":")
  let date = new Date()
  date.setHours(Number(hour), Number(minutes))
  rotations.forEach((rotation) => {
    const rotationTime = { startTime: getTimeFromDate(date) }
    date.setMinutes(date.getMinutes() + rotationDuration)
    rotationTime.endTime = getTimeFromDate(date)
    rotation.times = rotationTime
    date.setMinutes(date.getMinutes() + 5)
  });
}

function getTimeFromDate(date) {
  return date.getHours() + ":" + (date.getMinutes() === 0 ? "00" : date.getMinutes())
}

function adjustNumCourts(numCourts, numPlayers) {
  return Math.min(numCourts, Math.floor(numPlayers/4))
}
