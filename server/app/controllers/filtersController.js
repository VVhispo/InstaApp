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
        console.log(filterType)
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
        
        // sharp.cache(false)
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
        sleep(500)
        sharp(buffer).toFile(originalUrl.slice(0,-4) + "_filter.jpg");    
        return res
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