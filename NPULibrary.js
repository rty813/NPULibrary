var express = require('express');
var request = require('request');
var cheerio = require('cheerio');
var rp = require('request-promise');
var app = express();

var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.post('/npulibrary', function(req, res) {
    var bookname = encodeURIComponent(req.body.bookname);
    var page = req.body.page;
    console.log('POST ' + bookname + ' ' + page);
    res.setHeader('content-type', 'text/html;charset=utf-8');
    if (bookname) {
        let url = 'http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=' + bookname + '&page=' + page;
        var data = { "pageCount": 1, "data": [] };

        (async() => {
            try {
                let htmlBody = await rp(url);
                let $ = cheerio.load(htmlBody);
                $('span.pagination').first().find('b').find('font').each((index, elem) => {
                    if ($(elem).attr('color') === 'black') {
                        data['pageCount'] = $(elem).text();
                    }
                })

                var books = [];
                $('li.book_list_info').each((index, elem) => {
                    books[index] = $(elem);
                });
                for (let book of books) {
                    let name = book.find('h3').find('a').text();
                    let detail = book.find('p').text();
                    let url = 'http://202.117.255.187:8080/opac/' + book.find('h3').find('a').attr('href');
                    let imageUrl = '';
                    let $ = cheerio.load(await rp(url));
                    let isbn = '';
                    $('dl.booklist').each(function(i, elem) {
                        if ($(this).find('dt').first().text().indexOf('ISBN') >= 0) {
                            isbn = $(this).find('dd').text()
                            isbn = isbn.substring(0, isbn.indexOf('/')).replace('-', '');
                            // break;
                        }
                    });
                    let htmlBody = await rp('http://202.117.255.187:8080/opac/ajax_douban.php?isbn=' + isbn);
                    let jsonObj = JSON.parse(htmlBody);
                    imageUrl = jsonObj['image'];
                    if (imageUrl.indexOf('nobook') >= 0) {
                        imageUrl = '';
                    }

                    let placeStr = '';
                    $ = cheerio.load(await rp('http://202.117.255.187:8080/opac/ajax_' + book.find('h3').find('a').attr('href')));
                    $('tr').each((i, elem) => {
                        placeStr += $(elem).find('td').slice(3, 4).text();
                    });

                    data["data"].push({ "bookname": name, "detail": detail, "url": url, "imageUrl": imageUrl, "bookplace": placeStr });
                }
                res.end(JSON.stringify(data));
            } catch (e) {
                console.log(e);
            }
        })();
    } else if (error) {
        res.end(error);
    } else {
        res.end(response.statusCode);
    }
});


app.get('/npulibrary', function(req, res) {
    var bookname = encodeURIComponent(req.query.bookname);
    var page = req.query.page;
    console.log('GET ' + bookname + ' ' + page);
    res.setHeader('content-type', 'text/html;charset=utf-8');
    if (bookname) {
        let url = 'http://202.117.255.187:8080/opac/openlink.php?strSearchType=title&strText=' + bookname + '&page=' + page;
        var data = { "pageCount": 1, "data": [] };

        (async() => {
            try {
                let htmlBody = await rp(url);
                let $ = cheerio.load(htmlBody);
                $('span.pagination').first().find('b').find('font').each((index, elem) => {
                    if ($(elem).attr('color') === 'black') {
                        data['pageCount'] = $(elem).text();
                    }
                })

                var books = [];
                $('li.book_list_info').each((index, elem) => {
                    books[index] = $(elem);
                });
                for (let book of books) {
                    let name = book.find('h3').find('a').text();
                    let detail = book.find('p').text();
                    let url = 'http://202.117.255.187:8080/opac/' + book.find('h3').find('a').attr('href');
                    let imageUrl = '';
                    let $ = cheerio.load(await rp(url));
                    let isbn = '';
                    $('dl.booklist').each(function(i, elem) {
                        if ($(this).find('dt').first().text().indexOf('ISBN') >= 0) {
                            isbn = $(this).find('dd').text()
                            isbn = isbn.substring(0, isbn.indexOf('/')).replace('-', '');
                            // break;
                        }
                    });
                    let htmlBody = await rp('http://202.117.255.187:8080/opac/ajax_douban.php?isbn=' + isbn);
                    let jsonObj = JSON.parse(htmlBody);
                    imageUrl = jsonObj['image'];
                    if (imageUrl.indexOf('nobook') >= 0) {
                        imageUrl = '';
                    }

                    let placeStr = '';
                    $ = cheerio.load(await rp('http://202.117.255.187:8080/opac/ajax_' + book.find('h3').find('a').attr('href')));
                    $('tr').each((i, elem) => {
                        placeStr += $(elem).find('td').slice(3, 4).text();
                    });

                    data["data"].push({ "bookname": name, "detail": detail, "url": url, "imageUrl": imageUrl });
                }
                res.end(JSON.stringify(data));
            } catch (e) {
                console.log(e);
            }
        })();
    } else if (error) {
        res.end(error);
    } else {
        res.end(response.statusCode);
    }
});

app.listen(3000);
