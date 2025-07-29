// listens for search to be submitted
document.getElementsByClassName('.cmp-komatsu-form').addEventListener('submit', function(e) {
    e.preventDefault();
    const searchTerm = document.getElementById('searchTerm').value();

    fetch(`/bin/ckleinman/pagesearch?searchTerm=${encodeURIComponent(searchTerm)}`)
        .then(res => res.json())
        .then(data => {
            const searchResults = document.getElementsByClassName('.cmp-komatsu-form-search-results');
            searchResults.innerHTML = '';

            if (data.length == 0 ) {
                searchResults.innerHTML = `No results are returned for ${searchTerm}. Please try a different term`;
                return;
            }

            data.forEach(element => {
                const pageInfo = document.createElement("div");
                pageInfo.innerHTML = `
                    <h2>${element.title}</h2>
                    <0>${element.description}</p>
                    <img src='${element.image}' alt="" />
                    <p>${element.modifiedDate}</p>
                `;
                searchResults.append(pageInfo);
            });
        });
});