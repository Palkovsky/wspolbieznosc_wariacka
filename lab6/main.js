// Teoria Współbieżnośi, implementacja problemu 5 filozofów w node.js
// Opis problemu: http://en.wikipedia.org/wiki/Dining_philosophers_problem
//   https://pl.wikipedia.org/wiki/Problem_ucztuj%C4%85cych_filozof%C3%B3w
// 1. Dokończ implementację funkcji podnoszenia widelca (Fork.acquire).
// 2. Zaimplementuj "naiwny" algorytm (każdy filozof podnosi najpierw lewy, potem
//    prawy widelec, itd.).
// 3. Zaimplementuj rozwiązanie asymetryczne: filozofowie z nieparzystym numerem
//    najpierw podnoszą widelec lewy, z parzystym -- prawy.
// 4. Zaimplementuj rozwiązanie z kelnerem (według książki Programowanie współbiezne i rozproszone)
// 5. Zaimplementuj rozwiążanie z jednoczesnym podnoszeniem widelców:
//    filozof albo podnosi jednocześnie oba widelce, albo żadnego.
// 6. Uruchom eksperymenty dla różnej liczby filozofów i dla każdego wariantu
//    implementacji zmierz średni czas oczekiwania każdego filozofa na dostęp
//    do widelców. Wyniki przedstaw na wykresach.
function randInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

let Fork = function() {
    this.state = 0;
    return this;
};

Fork.prototype.acquire = function(cb) {
    // zaimplementuj funkcję acquire, tak by korzystala z algorytmu BEB
    // (http://pl.wikipedia.org/wiki/Binary_Exponential_Backoff), tzn:
    // 1. przed pierwszą próbą podniesienia widelca Filozof odczekuje 1ms
    // 2. gdy próba jest nieudana, zwiększa czas oczekiwania dwukrotnie
    //    i ponawia próbę, itd.
    /*
    // CSMA/CD like
    let beb = (cb, attempt, total) => {
        let delay = randInt(0, 1 << attempt) + 1;
        setTimeout(() => {
            total += delay;
            if(this.state != 0) {
                beb(cb, attempt + 1, total);
            } else {
                this.state = 1;
                cb(total);
            }
        }, delay);
    };
    beb(cb, 0, 0);
    */
    let beb = (cb, delay, total) => {
        setTimeout(() => {
            total += delay;
            if(this.state != 0) {
                beb(cb, delay*2, total);
            } else {
                this.state = 1;
                cb(total);
            }
        }, delay);
    };
    beb(cb, 1, 0);
};

Fork.prototype.acquire1 = function(cb) {
    let beb = (cb, total) => {
        let delay = 1;
        setTimeout(() => {
            total += delay;
            if(this.state != 0) {
                beb(cb,total);
            } else {
                this.state = 1;
                cb(total);
            }
        }, delay);
    };
    beb(cb, 0);
};

Fork.prototype.release = function() {
    this.state = 0;
};

let Conductor = function(forks) {
    this.forks = forks;
    this.allowed = {};
    this.queue = [];
    return this;
};

Conductor.prototype.maxAllowed = function() {
    return this.forks.length - 1;
};

Conductor.prototype.currentlyIn = function() {
    return Object.keys(this.allowed).length;
};

/*
 * phys_id - philosopher identifier
 * fork_id
 * cb - callback
 * extra_delay - extra field used to calculate wait time of requests coming back from queue
 */
Conductor.prototype.acquire = function(phys_id, fork_id, cb, timestamp) {
    timestamp = (timestamp === undefined) ? Date.now() : timestamp;

    // When there's too many contestants.
    if(this.currentlyIn() >= this.maxAllowed() && !(phys_id in this.allowed)) {
        let req = [phys_id, fork_id, cb, timestamp];
        this.queue.push(req);
        return;
    }

    // Count how many forks philosopher is contesting for
    if(phys_id in this.allowed) {
        this.allowed[phys_id] += 1;
    } else {
        this.allowed[phys_id] = 1;
    }

    // Perform actual fork.acquire()
    this.forks[fork_id].acquire1((delay) => {
        cb(Date.now() - timestamp);
    });
};

Conductor.prototype.release = function(phys_id, fork_id) {
    // Release the fork
    this.allowed[phys_id] -= 1;
    this.forks[fork_id].release();

    // When philosopher stopped contesting for forks
    // it is time to bring back one waiting in queue.
    if(this.allowed[phys_id] === 0) {
        delete this.allowed[phys_id];

        // There must be someting in queue to be brought back.
        if(this.queue.length === 0) {
            return;
        }

        let req = this.queue.shift(),
            req_phys_id = req[0],
            req_fork_id = req[1],
            req_cb = req[2],
            req_timestamp = req[3];
        this.acquire(req_phys_id, req_fork_id, req_cb, req_timestamp);
    }
};

let Philosopher = function(id, forks, conductor) {
    this.id = id;
    this.forks = forks;
    this.f1 = id % forks.length;
    this.f2 = (id + 1) % forks.length;
    this.conductor = conductor;
    return this;
};

// Eats for random amout of time.
// Eating time in range [10, 600)
Philosopher.prototype.sleep = function(cb) {
    let delay = Math.floor((Math.random()*501) + 100);
    setTimeout(() => cb(delay), delay);
};

Philosopher.prototype.eat = function(cb) {
    this.sleep(cb);
};

Philosopher.prototype.think = function(cb) {
    this.sleep(cb);
};

// 'Atomic' acquire of two forks.
Philosopher.prototype.acquirePair = function(f1, f2, cb) {
    let beb = (cb, delay, total) => {
        setTimeout(() => {
            total += delay;
            if(this.forks[f1].state != 0 || this.forks[f2].state != 0) {
                beb(cb, delay*2, total);
                return;
            }
            this.forks[f1].state = 1;
            this.forks[f2].state = 1;
            cb(total);
        }, delay);
    };
    beb(cb, 1, 0);
};

Philosopher.prototype.releasePair = function(f1, f2) {
    this.forks[f1].state = 0;
    this.forks[f2].state = 0;
};

Philosopher.prototype.startNaive = function(count) {
    let forks = this.forks,
        f1 = forks[this.f1],
        f2 = forks[this.f2],
        id = this.id;

    // zaimplementuj rozwiązanie naiwne
    // każdy filozof powinien 'count' razy wykonywać cykl
    // podnoszenia widelców -- jedzenia -- zwalniania widelców
    if(count === 0) {
        // console.log(`Philospoher ${id}: Finished`);
        return;
    }

    // console.log(`Philosopher ${id}: Start of ${count} cycle. Thinking...`);
    this.think((tt) => {
        // console.log(`Philosopher ${id}: Thinking done after ${tt}ms. Time to eat.`);
        f1.acquire((d1) => {
            // console.log(`Philospoher ${id}: Fork ${this.f1} acquired in ${d1}ms.`);
            f2.acquire((d2) => {
                console.log(`naive,${id},${d1}`);
                console.log(`naive,${id},${d2}`);
                // console.log(`Philospoher ${id}: Fork ${this.f2} acquired in ${d2}ms. Time for eating!`);
                this.eat((et) => {
                    // console.log(`Philospoher ${id}: Eating done after ${et}ms.`);
                    f1.release();
                    f2.release();
                    // console.log(`Philospoher ${id}: Both forks ${this.f1} and ${this.f2} released.`);
                    this.startNaive(count-1);
                });
            });
        });
    });
};

Philosopher.prototype.startAsym = function(count) {
    let forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;

    // zaimplementuj rozwiązanie asymetryczne
    // każdy filozof powinien 'count' razy wykonywać cykl
    // podnoszenia widelców -- jedzenia -- zwalniania widelców
    if(count === 0) {
        // console.log(`Philospoher ${id}: Finished`);
        return;
    }

    // console.log(`Philosopher ${id}: Start of ${count} cycle.`);
    let fork_fst_idx = (id % 2 == 1) ? f1 : f2,
        fork_lst_idx = (id % 2 == 1) ? f2 : f1,
        fork_fst = forks[fork_fst_idx],
        fork_lst = forks[fork_lst_idx];

    // console.log(`Philosopher ${id}: Start of ${count} cycle. Thinking...`);
    this.think((tt) => {
        // console.log(`Philosopher ${id}: Thinking done after ${tt}ms. Time to eat.`);
        fork_fst.acquire((d1) => {
            // console.log(`Philospoher ${id}: Fork ${fork_fst_idx} acquired in ${d1}ms.`);
            fork_lst.acquire((d2) => {
                console.log(`asym,${id},${d1}`);
                console.log(`asym,${id},${d2}`);
                // console.log(`Philospoher ${id}: Fork ${fork_lst_idx} acquired in ${d2}ms. Time for eating!`);
                this.eat((et) => {
                    // console.log(`Philospoher ${id}: Eating done after ${et}ms.`);
                    fork_fst.release();
                    fork_lst.release();
                    // console.log(`Philospoher ${id}: Both forks ${fork_fst_idx} and ${fork_lst_idx} released.`);
                    this.startAsym(count-1);
                });
            });
        });
    });
};

Philosopher.prototype.startConductor = function(count) {
    let conductor = this.conductor,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;

    // zaimplementuj rozwiązanie z kelnerem
    // każdy filozof powinien 'count' razy wykonywać cykl
    // podnoszenia widelców -- jedzenia -- zwalniania widelców
    if(count === 0) {
        // console.log(`Philospoher ${id}: Finished`);
        return;
    }

    // console.log(`Philosopher ${id}: Start of ${count} cycle. Thinking...`);
    this.think((tt) => {
        // console.log(`Philosopher ${id}: Thinking done after ${tt}ms. Time to eat.`);
        conductor.acquire(id, f1, (d1) => {
            // console.log(`Philospoher ${id}: Fork ${f1} acquired in ${d1}ms.`);
            conductor.acquire(id, f2, (d2) => {
                console.log(`conductor,${id},${d1}`);
                console.log(`conductor,${id},${d2}`);
                // console.log(`Philospoher ${id}: Fork ${f2} acquired in ${d2}ms. Time for eating!`);
                this.eat((et) => {
                    // console.log(`Philospoher ${id}: Eating done after ${et}ms.`);
                    conductor.release(id, f1);
                    conductor.release(id, f2);
                    // console.log(`Philospoher ${id}: Both forks ${f1} and ${f2} released.`);
                    this.startConductor(count-1);
                });
            });
        });
    });
};

// TODO: wersja z jednoczesnym podnoszeniem widelców
// Algorytm BEB powinien obejmować podnoszenie obu widelców,
// a nie każdego z osobna
Philosopher.prototype.startAtomPick = function(count) {
    let conductor = this.conductor,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;

    // zaimplementuj rozwiązanie z kelnerem
    // każdy filozof powinien 'count' razy wykonywać cykl
    // podnoszenia widelców -- jedzenia -- zwalniania widelców
    if(count === 0) {
        // console.log(`Philospoher ${id}: Finished`);
        return;
    }

    // console.log(`Philosopher ${id}: Start of ${count} cycle. Thinking...`);
    this.think((tt) => {
        // console.log(`Philosopher ${id}: Thinking done after ${tt}ms. Time to eat.`);
        this.acquirePair(f1, f2, (delay) => {
            console.log(`atompick,${id},${delay}`);
            // console.log(`Philospoher ${id}: Forks ${f1} and ${f2} acquired in ${delay}ms. Time for eating!`);
            this.eat((et) => {
                // console.log(`Philospoher ${id}: Eating done after ${et}ms.`);
                this.releasePair(f1, f2);
                // console.log(`Philospoher ${id}: Both forks ${f1} and ${f2} released.`);
                this.startAtomPick(count-1);
            });
        });
    });
};

if(process.argv.length < 4) {
    console.log(`Usage: ${process.argv[1]} <N> <naive|asym|cond|atompick>`);
    process.exit(1);
}

let N = parseInt(process.argv[2]);
let forks = [];
let philosophers = [];

for(let i = 0; i < N; i++) {
    forks.push(new Fork());
}

let conductor = new Conductor(forks);
for (let i = 0; i < N; i++) {
    philosophers.push(new Philosopher(i, forks, conductor));
}

let option = process.argv[3];
for (let i = 0; i < N; i++) {
    switch(option) {
    case "naive":
        philosophers[i].startNaive(20);
        break;
    case "asym":
        philosophers[i].startAsym(20);
        break;
    case "cond":
        philosophers[i].startConductor(20);
        break;
    case "atompick":
        philosophers[i].startAtomPick(20);
        break;
    default:
        console.log(`Usage: ${process.argv[1]} <N> <naive|asym|cond|atompick>`);
        process.exit(1);
        break;
    }
}
