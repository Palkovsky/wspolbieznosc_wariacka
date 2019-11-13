
function printAsync(s, cb) {
    var delay = Math.floor((Math.random()*1000)+500);
    setTimeout(function() {
        console.log(s);
        if (cb) cb();
    }, delay);
}

function task1(cb) {
    printAsync("1", function() {
        task2(cb);
    });
}

function task2(cb) {
    printAsync("2", function() {
        task3(cb);
    });
}

function task3(cb) {
    printAsync("3", cb);
}

/*
// wywolanie sekwencji zadan
task1(function() {
    console.log('done!');
});
*/

/*
** Zadanie:
** Napisz funkcje loop(n), ktora powoduje wykonanie powyzszej
** sekwencji zadan n razy.
**
*/

/*
 * Zadanie 1a)
 */
function loop_a(n) {
    if(n === 0) {
        console.log("done!");
    } else {
        task1(() => loop_a(n-1));
    }
}
// loop_a(4);

/*
 * Zadanie 1b)
 */
const async = require("async");
function loop_b(n) {
    let tasks = [...Array(n).keys()].map((_) => {
        return (nextTask) => task1(() => nextTask());
    });
    async.waterfall(tasks, () => console.log("done!"));
}
loop_b(4);
