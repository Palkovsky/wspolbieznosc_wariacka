const walk = require('walkdir');
const fs = require('fs');
const async = require("async");

function linecount(file, ok, error) {
    // console.log("linecout %s", file);
    let count = 0;
    fs.createReadStream(file).on('data', (chunk) => {
        count += chunk.toString('utf8')
            .split(/\r\n|[\n\r\u0085\u2028\u2029]/g)
            .length-1;
    }).on('end', () => {
        if(ok) { ok(count); }
    }).on('error', (err) => {
        if(error) { error(err); }
    });
}

// Taken from https://stackoverflow.com/a/40163759
function S_ISREG(mode) {
    const S_IFREG = 0100000;
    const S_IFMT = 0170000;
    return ((mode) & S_IFMT) == S_IFREG;
}

/*
 * In this soulution I acquire whole path list before processing.
 */
function synchronous(root, ok) {
    let objects = walk.sync(root, {"return_object": true});
    let paths = Object.keys(objects);

    // This recursion is a bit funny, due to asynchronous nature of linecount()
    // but we call count_lines() AFTER processing single file, so it is processed one by one.
    let count_lines = (count, i, ok) => {
        if (i < 0) {
            ok(count);
            return;
        }

        let path = paths[i];
        let stat = objects[path];

        if(!S_ISREG(stat.mode)) {
            count_lines(count, i-1, ok);
            return;
        }

        linecount(path,
                  cnt => count_lines(count+cnt, i-1, ok),
                  err => console.log(err));

    };

    count_lines(0, paths.length-1, ok);
}

function asynchronous(root, ok) {
    let count = 0;
    let files_counted = 0;
    let files_total = 0;
    let emitter = walk(root);

    let shared_cb = (cnt) => {
        files_counted += 1;
        count += cnt;
        if(files_counted == files_total) {
            ok(count);
        }
    };

    emitter.on("file", (path, stat) => {
        files_total += 1;
        linecount(path, shared_cb, console.log);
    });
}

let hrstart = process.hrtime();
<?
  $sync = getopt('s:')['s'];
  if ($sync == 'sync') {
?>

synchronous("./PAM08", (count) => {
    let hrend = process.hrtime(hrstart);
    let millis = Math.round(hrend[0]*1e3 + hrend[1]/1e6);
    console.log("sync, %d, %d", millis, count);
});

<? } else { ?>

asynchronous("./PAM08", (count) => {
    let hrend = process.hrtime(hrstart);
    let millis = Math.round(hrend[0]*1e3 + hrend[1]/1e6);
    console.log("async, %d, %d", millis, count);
});

<?  } ?>
