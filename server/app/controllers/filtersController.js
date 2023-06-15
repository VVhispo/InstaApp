const fs = require('fs');
const sharp = require("sharp");
const IC = require("./imageController")



module.exports = {
    getPhotoMeta: async(id) => {
        try {
            const photo = IC.getPhoto(id)
            if (!photo) return JSON.stringify({error: "Photo not found"})

            const url = JSON.parse(photo).url
            const metadata =  await sharp(url).metadata();
            return JSON.stringify(metadata)
        } catch (err) {
            return JSON.stringify({error: err.message})
        }
    },
    applyFilter: async(data) => {
        const {id, filterType, dimensions, tint} = data
        const photo = IC.getPhoto(id)
        if(JSON.parse(photo).error) return photo
        const originalUrl = JSON.parse(photo).url
        let url = IC.getFilteredUrl(id)
        if(!url) url = originalUrl
        else url = JSON.parse(url)
        const res = await IC.applyFilterUpdate({
            id: JSON.parse(photo).id,
            status: filterType,
        })
        console.log(url)
        sharp.cache(false)
        let buffer;
        switch(filterType){
            case "flip":
                buffer = await sharp(url)
                    .flop()
                    .toBuffer()
                break;
            case "flop":
                buffer = await sharp(url)
                    .flip()
                    .toBuffer()
                break;
            case "resize":
                if(!dimensions || !dimensions.width || !dimensions.height) return JSON.stringify({error: "Dimensions not specified"})
                buffer = await sharp(url)
                .resize({
                    width: dimensions.width,
                    height: dimensions.height
                })
                .toBuffer()
                break;
            case "tint":
                if(!tint) return JSON.stringify({error: "Color values not specified"})
                buffer = await sharp(url)
                .tint({
                    r:tint.r,
                    g:tint.g,
                    b:tint.b
                })
                .toBuffer()
                break;
            case "grayscale":
                buffer = await sharp(url)
                    .grayscale()
                    .toBuffer()
                break;
            default: return JSON.stringify({error: "Filter unknown"});
        }
        sleep(3500)
        sharp(buffer).toFile(originalUrl.slice(0,-4) + "_filter.jpg");    
        return res
    },
    applyFilters: async(data) => {
        const {id, filters, tint} = data
        const photo = IC.getPhoto(id)
        if(JSON.parse(photo).error) return photo
        const originalUrl = JSON.parse(photo).url
        const res = await IC.applyFilterUpdate({
            id: JSON.parse(photo).id,
            status: filters.join("_"),
        })
        console.log(filters)
        const newUrl = originalUrl.slice(0,-4) + "_filter.jpg";
        if(filters.length == 1){
            if(filters[0] == "grayscale") await sharp(originalUrl).grayscale().withMetadata().toFile(newUrl);
            if(filters[0] == "flip") await sharp(originalUrl).flip().withMetadata().toFile(newUrl);
            if(filters[0] == "flop") await sharp(originalUrl).flop().withMetadata().toFile(newUrl);
            if(filters[0] == "tint") await sharp(originalUrl).tint(tint).withMetadata().toFile(newUrl);
        }else if(filters.length == 3){
            if(filters.includes("grayscale")) await sharp(originalUrl).flip().flop().grayscale().withMetadata().toFile(newUrl);
            if(filters.includes("tint")) await sharp(originalUrl).flip().flop().tint(tint).withMetadata().toFile(newUrl);
        }else if(filters.length == 2){
            if(filters.includes("flip") && filters.includes("flop")) await sharp(originalUrl).flip().flop().withMetadata().toFile(newUrl);
            
            if(filters.includes("flip") && filters.includes("grayscale")) await sharp(originalUrl).flip().grayscale().withMetadata().toFile(newUrl);
            if(filters.includes("flip") && filters.includes("tint")) await sharp(originalUrl).flip().tint(tint).withMetadata().toFile(newUrl);

            if(filters.includes("flop") && filters.includes("grayscale")) await sharp(originalUrl).flop().grayscale().withMetadata().toFile(newUrl);
            if(filters.includes("flop") && filters.includes("tint")) await sharp(originalUrl).flop().tint(tint).withMetadata().toFile(newUrl);
        }
        return res;
    }
}

function sleep(milliseconds) {
    var start = new Date().getTime();
    for (var i = 0; i < 1e7; i++) {
      if ((new Date().getTime() - start) > milliseconds){
        break;
      }
    }
  }