# Static Page Generator

[![Build Status](https://api.travis-ci.org/Synesso/sbt-spg.png)](https://travis-ci.org/Synesso/sbt-spg)

A work in progress, sbt-spg is an SBT plugin that generates static sites from markdown.

## Building

To build and publish locally: 

    sbt test publishLocal
    
## Using

In your site's project define a project/plugins.sbt file containing:

    addSbtPlugin("com.github.synesso" % "sbt-spg" % "0.1.0-SNAPSHOT")
    
The markdown content belongs in the path `src/main/site/_articles/` and can be in subdirectories, which will be preserved.

Generate the site to `target` with `sbt spgGenerate`.

### Article meta-data

Content may include a frontmatter section that declares metadata for the article. For example, in the markdown file with this content:

    ---
    tags = [this,that,the other thing]
    ---
    # Here I wish to show you
    
    ## {tags}
    
    
The HOCON configuration between the triple-dashes is parsed and available for use in the markdown content. This renders as:

    <h1>Here I wish to show you</h1>
    <h2>that, the other thing, this</h2>